package com.github.darnoker.orderservice.order.model;

import com.github.darnoker.orderservice.order.OrderStatus;
import com.github.darnoker.orderservice.order.CurrencyCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Order(UUID id, UUID customerId, List<OrderItem> items, OrderStatus status,
                    BigDecimal totalAmount, CurrencyCode currency, Instant createdAt, Instant updatedAt) {

    public Order {
        id = Objects.requireNonNull(id, "id must not be null");
        customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (items.isEmpty()) throw new IllegalArgumentException("items must not be empty");
        status = Objects.requireNonNull(status, "status must not be null");
        totalAmount = Objects.requireNonNull(totalAmount, "totalAmount must not be null");
        currency = Objects.requireNonNull(currency, "currency must not be null");
        createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }
}
