package com.github.darnoker.productservice.inventory;

import com.github.darnoker.productservice.inventory.model.ReservationResult;

import java.util.List;
import java.util.UUID;

record StockReservedEvent(UUID orderId, List<ReservationResult> reservations) {
}
