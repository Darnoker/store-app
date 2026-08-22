package com.github.darnoker.orderservice.order.model;

import com.github.darnoker.orderservice.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Order(UUID id, Long customerId, Long productId, int quantity, BigDecimal price, OrderStatus status, Instant createdAt) {

    public Order {
        id = Objects.requireNonNull(id, "id must not be null");
        requirePositive(customerId, "customerId");
        requirePositive(productId, "productId");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (Objects.requireNonNull(price, "price must not be null").signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
        status = Objects.requireNonNull(status, "status must not be null");
        createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    private static void requirePositive(Long value, String name) {
        if (Objects.requireNonNull(value, name + " must not be null") <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
