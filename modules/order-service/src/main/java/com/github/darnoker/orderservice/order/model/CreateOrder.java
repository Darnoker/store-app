package com.github.darnoker.orderservice.order.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreateOrder(UUID customerId, List<CreateOrderItem> items) {

    public CreateOrder {
        customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
    }
}
