package com.github.darnoker.orderservice.order.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record OrderItem(UUID id, UUID productId, String productType, String productName, BigDecimal unitPrice, int quantity) {

    public OrderItem {
        id = Objects.requireNonNull(id, "id must not be null");
        productId = Objects.requireNonNull(productId, "productId must not be null");
        productType = Objects.requireNonNull(productType, "productType must not be null");
        productName = Objects.requireNonNull(productName, "productName must not be null");
        unitPrice = Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (unitPrice.signum() <= 0 || quantity <= 0) throw new IllegalArgumentException("price and quantity must be positive");
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
