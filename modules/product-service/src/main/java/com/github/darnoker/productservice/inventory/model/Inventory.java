package com.github.darnoker.productservice.inventory.model;

import java.time.Instant;
import java.util.UUID;

public record Inventory(UUID productId, int quantity, int reservedQuantity, Instant updatedAt) {
}
