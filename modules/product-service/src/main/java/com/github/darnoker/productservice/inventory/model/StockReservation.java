package com.github.darnoker.productservice.inventory.model;

import com.github.darnoker.productservice.inventory.StockReservationStatus;

import java.time.Instant;
import java.util.UUID;

public record StockReservation(
        UUID id,
        UUID productId,
        UUID orderId,
        UUID requestId,
        Quantity quantity,
        StockReservationStatus status,
        Instant expiresAt,
        Instant createdAt) {

    public StockReservation updateStatus(StockReservationStatus status) {
        return new StockReservation(id, productId, orderId, requestId, quantity, status, expiresAt, createdAt);
    }
}
