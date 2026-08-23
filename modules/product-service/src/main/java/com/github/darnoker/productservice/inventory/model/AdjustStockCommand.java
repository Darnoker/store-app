package com.github.darnoker.productservice.inventory.model;

import java.util.UUID;

public record AdjustStockCommand(UUID productId, int quantityChange, String reason, UUID requestId) {
}
