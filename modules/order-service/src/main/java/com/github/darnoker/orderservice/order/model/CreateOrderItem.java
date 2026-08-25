package com.github.darnoker.orderservice.order.model;

import java.util.Objects;
import java.util.UUID;

public record CreateOrderItem(UUID productId, int quantity) {

    public CreateOrderItem {
        productId = Objects.requireNonNull(productId, "productId must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
