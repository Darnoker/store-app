package com.github.darnoker.orderservice.order.model;

import java.math.BigDecimal;
import java.util.Objects;

public record CreateOrder(Long customerId, Long productId, Integer quantity, BigDecimal price) {

    public CreateOrder {
        requirePositive(customerId, "customerId");
        requirePositive(productId, "productId");
        requirePositive(quantity, "quantity");
        if (Objects.requireNonNull(price, "price must not be null").signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
    }

    private static void requirePositive(Number value, String name) {
        if (Objects.requireNonNull(value, name + " must not be null").longValue() <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
