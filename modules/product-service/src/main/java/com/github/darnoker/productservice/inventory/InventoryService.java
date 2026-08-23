package com.github.darnoker.productservice.inventory;

import com.github.darnoker.productservice.inventory.model.AdjustStockCommand;
import com.github.darnoker.productservice.inventory.model.ConfirmReservationsCommand;
import com.github.darnoker.productservice.inventory.model.ExpireReservationsCommand;
import com.github.darnoker.productservice.inventory.model.ReleaseReservationsCommand;
import com.github.darnoker.productservice.inventory.model.ReservationResult;
import com.github.darnoker.productservice.inventory.model.ReserveStockCommand;
import com.github.darnoker.productservice.inventory.model.ReservedItem;
import com.github.darnoker.productservice.inventory.persistence.InventoryEntity;
import com.github.darnoker.productservice.inventory.persistence.InventoryRepository;
import com.github.darnoker.productservice.inventory.persistence.StockReservationEntity;
import com.github.darnoker.productservice.inventory.persistence.StockReservationRepository;
import com.github.darnoker.productservice.outbox.persistence.OutboxEventEntity;
import com.github.darnoker.productservice.outbox.persistence.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    private final StockReservationRepository stockReservationRepository;

    private final OutboxEventRepository outboxEventRepository;

    private final Clock clock;

    @Transactional
    public List<ReservationResult> reserveStock(ReserveStockCommand command) {
        List<StockReservationEntity> existingReservations = stockReservationRepository
                .findAllByOrderIdAndRequestId(command.orderId(), command.requestId());
        if (!existingReservations.isEmpty()) {
            return existingReservations.stream()
                    .map(this::toReservationResult)
                    .toList();
        }

        Map<UUID, Integer> productIdByQuantity = command.reservedItems().stream()
                .collect(Collectors.toMap(ReservedItem::productId, ReservedItem::quantity, Integer::sum));

        List<InventoryEntity> inventories = inventoryRepository.findAllByProductIdInForUpdate(productIdByQuantity.keySet());
        if (inventories.size() != productIdByQuantity.size()) {
            throw new IllegalArgumentException("Inventory does not exist for every requested product");
        }
        Instant now = Instant.now(clock);
        for (InventoryEntity inventory : inventories) {
            int requestedQuantity = productIdByQuantity.get(inventory.getProductId());
            if (inventory.getQuantity() - inventory.getReservedQuantity() < requestedQuantity) {
                throw new IllegalStateException("Insufficient stock for product " + inventory.getProductId());
            }
        }
        inventories.forEach(inventory -> inventory.update(inventory.getQuantity(),
                inventory.getReservedQuantity() + productIdByQuantity.get(inventory.getProductId()), now));

        Instant expiresAt = now.plusSeconds(15 * 60L);
        List<StockReservationEntity> reservations = inventories.stream()
                .map(inventory -> new StockReservationEntity(
                        UUID.randomUUID(),
                        inventory.getProductId(),
                        command.orderId(),
                        command.requestId(),
                        productIdByQuantity.get(inventory.getProductId()),
                        StockReservationStatus.RESERVED,
                        expiresAt,
                        now))
                .toList();
        stockReservationRepository.saveAll(reservations);
        reservations.forEach(reservation -> publish(reservation.getOrderId(), InventoryEventType.STOCK_RESERVED, now));

        return reservations.stream()
                .map(this::toReservationResult)
                .toList();
    }

    @Transactional
    public void confirmReservations(ConfirmReservationsCommand command) {
        transitionReservations(command.orderId(), StockReservationStatus.CONFIRMED, Instant.now(clock));
    }

    @Transactional
    public void releaseReservations(ReleaseReservationsCommand command) {
        transitionReservations(command.orderId(), StockReservationStatus.RELEASED, Instant.now(clock));
    }

    @Transactional
    public void expireReservations(ExpireReservationsCommand command) {
        Instant now = Instant.now(clock);
        transitionReservations(
                stockReservationRepository.findAllByStatusAndExpiresAtLessThanEqual(StockReservationStatus.RESERVED, now),
                StockReservationStatus.EXPIRED,
                now);
    }

    @Transactional
    public void adjustStock(AdjustStockCommand command) {
        if (command.quantityChange() == 0) {
            throw new InvalidStockAdjustmentException("Stock adjustment must not be zero");
        }
        Instant now = Instant.now(clock);
        InventoryEntity inventory = findInventoryForUpdate(command.productId());
        int adjustedQuantity = inventory.getQuantity() + command.quantityChange();
        if (adjustedQuantity < inventory.getReservedQuantity()) {
            throw new StockAdjustmentBelowReservedQuantityException();
        }
        inventory.update(adjustedQuantity, inventory.getReservedQuantity(), now);
        publish(command.productId(), InventoryEventType.STOCK_ADJUSTED, now);
    }

    private void transitionReservations(UUID orderId, StockReservationStatus targetStatus, Instant now) {
        transitionReservations(stockReservationRepository.findAllByOrderIdAndStatus(orderId, StockReservationStatus.RESERVED),
                targetStatus,
                now);
    }

    private void transitionReservations(Collection<StockReservationEntity> reservations,
                                        StockReservationStatus targetStatus,
                                        Instant now) {
        if (reservations.isEmpty()) {
            return;
        }

        Map<UUID, InventoryEntity> inventoriesByProductId = inventoryRepository.findAllByProductIdInForUpdate(
                        reservations.stream()
                                .map(StockReservationEntity::getProductId)
                                .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(InventoryEntity::getProductId, inventory -> inventory));

        for (StockReservationEntity reservation : reservations) {
            InventoryEntity inventory = inventoriesByProductId.get(reservation.getProductId());
            if (inventory == null) {
                throw new IllegalArgumentException("Inventory does not exist for product " + reservation.getProductId());
            }
            if (targetStatus == StockReservationStatus.CONFIRMED) {
                inventory.update(inventory.getQuantity() - reservation.getQuantity(), inventory.getReservedQuantity() - reservation.getQuantity(), now);
            } else {
                inventory.update(inventory.getQuantity(), inventory.getReservedQuantity() - reservation.getQuantity(), now);
            }
            reservation.updateStatus(targetStatus);
            publish(reservation.getOrderId(), eventTypeFor(targetStatus), now);
        }
    }

    private InventoryEntity findInventoryForUpdate(UUID productId) {
        return inventoryRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory does not exist for product " + productId));
    }

    private InventoryEventType eventTypeFor(StockReservationStatus status) {
        return switch (status) {
            case CONFIRMED -> InventoryEventType.STOCK_CONFIRMED;
            case RELEASED -> InventoryEventType.STOCK_RELEASED;
            case EXPIRED -> InventoryEventType.STOCK_EXPIRED;
            case RESERVED -> throw new IllegalArgumentException("Reserved status is not a reservation transition");
        };
    }

    private void publish(UUID aggregateId, InventoryEventType eventType, Instant now) {
        outboxEventRepository.save(new OutboxEventEntity(UUID.randomUUID(), aggregateId, eventType.value(), "{}", now, false));
    }

    private ReservationResult toReservationResult(StockReservationEntity reservation) {
        return new ReservationResult(
                reservation.getProductId(),
                reservation.getQuantity(),
                reservation.getId(),
                LocalDateTime.ofInstant(reservation.getExpiresAt(), clock.getZone()));
    }

}
