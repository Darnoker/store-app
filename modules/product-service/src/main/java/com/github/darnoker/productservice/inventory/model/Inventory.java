package com.github.darnoker.productservice.inventory.model;

import java.time.Instant;
import java.util.UUID;

public record Inventory(UUID productId, Quantity quantity, Quantity reservedQuantity, Instant updatedAt) {

    public Inventory {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (quantity == null || reservedQuantity == null) {
            throw new IllegalArgumentException("Inventory quantities are required");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("Updated time is required");
        }
        if (quantity.isLessThan(reservedQuantity)) {
            throw new IllegalArgumentException("Reserved quantity must not exceed quantity");
        }
    }

    public Inventory update(Quantity quantity, Quantity reservedQuantity, Instant updatedAt) {
        return new Inventory(productId, quantity, reservedQuantity, updatedAt);
    }
}
