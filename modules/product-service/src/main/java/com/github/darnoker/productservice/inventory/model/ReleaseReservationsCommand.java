package com.github.darnoker.productservice.inventory.model;

import java.util.UUID;

public record ReleaseReservationsCommand(UUID orderId, UUID requestId, String reason) {
}
