package com.github.darnoker.productservice.inventory.model;

import java.util.UUID;

public record ConfirmReservationsCommand(UUID orderId, UUID requestId) {
}
