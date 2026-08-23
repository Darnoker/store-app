package com.github.darnoker.productservice.inventory.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResult(UUID productId, int quantity, UUID reservationId, LocalDateTime expiresAt) {
}
