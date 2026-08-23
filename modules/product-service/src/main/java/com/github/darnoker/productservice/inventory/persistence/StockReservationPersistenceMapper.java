package com.github.darnoker.productservice.inventory.persistence;

import com.github.darnoker.productservice.inventory.model.Quantity;
import com.github.darnoker.productservice.inventory.model.StockReservation;

final class StockReservationPersistenceMapper {

    private StockReservationPersistenceMapper() {
    }

    static StockReservation toDomain(StockReservationEntity entity) {
        return new StockReservation(
                entity.getId(),
                entity.getProductId(),
                entity.getOrderId(),
                entity.getRequestId(),
                new Quantity(entity.getQuantity()),
                entity.getStatus(),
                entity.getExpiresAt(),
                entity.getCreatedAt());
    }

    static StockReservationEntity toEntity(StockReservation reservation) {
        return new StockReservationEntity(
                reservation.id(),
                reservation.productId(),
                reservation.orderId(),
                reservation.requestId(),
                reservation.quantity().value(),
                reservation.status(),
                reservation.expiresAt(),
                reservation.createdAt());
    }
}
