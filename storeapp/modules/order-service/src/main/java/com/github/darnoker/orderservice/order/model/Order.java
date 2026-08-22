package com.github.darnoker.orderservice.order.model;

import com.github.darnoker.orderservice.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Order(UUID id, Long customerId, Long productId, int quantity, BigDecimal price, OrderStatus status, Instant createdAt) {
}
