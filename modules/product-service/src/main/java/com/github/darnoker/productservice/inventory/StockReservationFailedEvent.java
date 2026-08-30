package com.github.darnoker.productservice.inventory;

import java.util.List;
import java.util.UUID;

record StockReservationFailedEvent(UUID orderId, String reason, List<FailedReservationItem> items) {
}
