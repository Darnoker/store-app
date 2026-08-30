package com.github.darnoker.productservice.inventory;

import java.util.UUID;

record FailedReservationItem(UUID productId, int quantity) {
}
