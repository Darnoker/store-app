package com.github.darnoker.productservice.inventory.model;

import java.util.Collection;
import java.util.UUID;

public record ReserveStockCommand(UUID orderId, UUID requestId, Collection<ReservedItem> reservedItems) {
}