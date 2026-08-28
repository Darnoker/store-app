package com.github.darnoker.productservice.inventory;

import com.github.darnoker.productservice.inventory.model.AdjustStockCommand;
import com.github.darnoker.productservice.inventory.model.ConfirmReservationsCommand;
import com.github.darnoker.productservice.inventory.model.Inventory;
import com.github.darnoker.productservice.inventory.model.Quantity;
import com.github.darnoker.productservice.inventory.model.ReleaseReservationsCommand;
import com.github.darnoker.productservice.inventory.model.ReservationResult;
import com.github.darnoker.productservice.inventory.model.ReserveStockCommand;
import com.github.darnoker.productservice.inventory.model.ReservedItem;
import com.github.darnoker.productservice.inventory.model.StockReservation;
import com.github.darnoker.productservice.inventory.persistence.InventoryRepository;
import com.github.darnoker.productservice.inventory.persistence.StockReservationRepository;
import com.github.darnoker.productservice.outbox.persistence.OutboxEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    private final StockReservationRepository stockReservationRepository;

    private final OutboxEventPublisher outboxEventPublisher;

    private final Clock clock;

    @Transactional
    public List<ReservationResult> reserveStock(ReserveStockCommand command) {
        log.info("Reserving stock for order {} with request {}", command.orderId(), command.requestId());
        List<StockReservation> existingReservations = stockReservationRepository
                .findAllByOrderIdAndRequestId(command.orderId(), command.requestId());
        if (!existingReservations.isEmpty()) {
            log.info("Returning {} existing stock reservation(s) for order {}", existingReservations.size(), command.orderId());
            return existingReservations.stream()
                    .map(this::toReservationResult)
                    .toList();
        }

        Map<UUID, Quantity> quantityByProductId = command.reservedItems().stream()
                .collect(Collectors.toMap(ReservedItem::productId, ReservedItem::quantity, Quantity::add));

        List<Inventory> inventories = inventoryRepository.findAllByProductIdInForUpdate(quantityByProductId.keySet());
        if (inventories.size() != quantityByProductId.size()) {
            log.warn("Cannot reserve stock for order {}: inventory is missing", command.orderId());
            throw new IllegalArgumentException("Inventory does not exist for every requested product");
        }
        Instant now = Instant.now(clock);
        for (Inventory inventory : inventories) {
            Quantity requestedQuantity = quantityByProductId.get(inventory.productId());
            if (inventory.quantity().subtract(inventory.reservedQuantity()).isLessThan(requestedQuantity)) {
                log.warn("Cannot reserve {} units of product {} for order {} due to insufficient stock", requestedQuantity.value(), inventory.productId(), command.orderId());
                throw new IllegalStateException("Insufficient stock for product " + inventory.productId());
            }
        }
        inventories = inventories.stream()
                .map(inventory -> inventory.update(inventory.quantity(),
                        inventory.reservedQuantity().add(quantityByProductId.get(inventory.productId())), now))
                .toList();
        inventoryRepository.saveAll(inventories);

        Instant expiresAt = now.plusSeconds(15 * 60L);
        List<StockReservation> reservations = inventories.stream()
                .map(inventory -> new StockReservation(
                        UUID.randomUUID(),
                        inventory.productId(),
                        command.orderId(),
                        command.requestId(),
                        quantityByProductId.get(inventory.productId()),
                        StockReservationStatus.RESERVED,
                        expiresAt,
                        now))
                .toList();
        stockReservationRepository.saveAll(reservations);
        reservations.forEach(reservation -> publish(reservation.orderId(), InventoryEventType.STOCK_RESERVED, now));
        log.info("Reserved stock for order {}", command.orderId());

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
    public void expireReservations() {
        Instant now = Instant.now(clock);
        transitionReservations(
                stockReservationRepository.findAllByStatusAndExpiresAtLessThanEqual(StockReservationStatus.RESERVED, now),
                StockReservationStatus.EXPIRED,
                now);
    }

    @Transactional
    public void adjustStock(AdjustStockCommand command) {
        log.info("Adjusting stock for product {} by {}", command.productId(), command.quantityChange());
        if (command.quantityChange() == 0) {
            log.warn("Rejected zero stock adjustment for product {}", command.productId());
            throw new InvalidStockAdjustmentException("Stock adjustment must not be zero");
        }
        Instant now = Instant.now(clock);
        Inventory inventory = findInventoryForUpdate(command.productId());
        Quantity adjustedQuantity = inventory.quantity().add(command.quantityChange());
        if (adjustedQuantity.isLessThan(inventory.reservedQuantity())) {
            throw new StockAdjustmentBelowReservedQuantityException();
        }
        inventoryRepository.save(inventory.update(adjustedQuantity, inventory.reservedQuantity(), now));
        publish(command.productId(), InventoryEventType.STOCK_ADJUSTED, now);
    }

    private void transitionReservations(UUID orderId, StockReservationStatus targetStatus, Instant now) {
        transitionReservations(stockReservationRepository.findAllByOrderIdAndStatus(orderId, StockReservationStatus.RESERVED),
                targetStatus,
                now);
    }

    private void transitionReservations(Collection<StockReservation> reservations,
                                        StockReservationStatus targetStatus,
                                        Instant now) {
        if (reservations.isEmpty()) {
            return;
        }

        Map<UUID, Inventory> inventoriesByProductId = inventoryRepository.findAllByProductIdInForUpdate(
                        reservations.stream()
                                .map(StockReservation::productId)
                                .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Inventory::productId, inventory -> inventory));

        for (StockReservation reservation : reservations) {
            Inventory inventory = inventoriesByProductId.get(reservation.productId());
            if (inventory == null) {
                throw new IllegalArgumentException("Inventory does not exist for product " + reservation.productId());
            }
            if (targetStatus == StockReservationStatus.CONFIRMED) {
                inventoriesByProductId.put(inventory.productId(), inventory.update(
                        inventory.quantity().subtract(reservation.quantity()),
                        inventory.reservedQuantity().subtract(reservation.quantity()),
                        now));
            } else {
                inventoriesByProductId.put(inventory.productId(), inventory.update(
                        inventory.quantity(),
                        inventory.reservedQuantity().subtract(reservation.quantity()),
                        now));
            }
            publish(reservation.orderId(), eventTypeFor(targetStatus), now);
        }
        inventoryRepository.saveAll(inventoriesByProductId.values());
        stockReservationRepository.saveAll(reservations.stream()
                .map(reservation -> reservation.updateStatus(targetStatus))
                .toList());
    }

    private Inventory findInventoryForUpdate(UUID productId) {
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
        outboxEventPublisher.publish(aggregateId, eventType.value(), "{}", now);
    }

    private ReservationResult toReservationResult(StockReservation reservation) {
        return new ReservationResult(
                reservation.productId(),
                reservation.quantity().value(),
                reservation.id(),
                LocalDateTime.ofInstant(reservation.expiresAt(), clock.getZone()));
    }

}
