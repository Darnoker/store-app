package com.github.darnoker.productservice.inventory;

import com.github.darnoker.productservice.inventory.model.Inventory;
import com.github.darnoker.productservice.inventory.model.ReservationResult;
import com.github.darnoker.productservice.inventory.model.ReserveStockCommand;
import com.github.darnoker.productservice.inventory.model.ReservedItem;
import com.github.darnoker.productservice.inventory.persistence.InventoryEntity;
import com.github.darnoker.productservice.inventory.persistence.InventoryRepository;
import com.github.darnoker.productservice.inventory.persistence.StockReservationEntity;
import com.github.darnoker.productservice.inventory.persistence.StockReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    private final StockReservationRepository stockReservationRepository;

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

        List<Inventory> inventories = findInventorysByProductIds(productIdByQuantity.keySet());
        Instant now = Instant.now(clock);

        List<Inventory> updatedInventories = inventories.stream()
                .map(inventory -> {
                    int quantity = productIdByQuantity.getOrDefault(inventory.productId(), 0);
                    return new Inventory(inventory.productId(), inventory.quantity(), inventory.reservedQuantity() + quantity, now);
                })
                .toList();

        List<InventoryEntity> updatedEntites = updatedInventories.stream()
                .map(InventoryMapper::toPersistence)
                .toList();

        inventoryRepository.saveAll(updatedEntites);

        Instant expiresAt = now.plusSeconds(15 * 60L);
        List<StockReservationEntity> reservations = updatedInventories.stream()
                .map(inventory -> new StockReservationEntity(
                        UUID.randomUUID(),
                        inventory.productId(),
                        command.orderId(),
                        command.requestId(),
                        productIdByQuantity.getOrDefault(inventory.productId(), 0),
                        StockReservationStatus.RESERVED,
                        expiresAt,
                        now))
                .toList();
        stockReservationRepository.saveAll(reservations);

        return reservations.stream()
                .map(this::toReservationResult)
                .toList();
    }

    private List<Inventory> findInventorysByProductIds(Collection<UUID> productIds) {
        return inventoryRepository.findAllByProductIdIn(productIds).stream()
                .map(InventoryMapper::toDomain)
                .toList();
    }

    private ReservationResult toReservationResult(StockReservationEntity reservation) {
        return new ReservationResult(
                reservation.getProductId(),
                reservation.getQuantity(),
                reservation.getId(),
                LocalDateTime.ofInstant(reservation.getExpiresAt(), clock.getZone()));
    }

}
