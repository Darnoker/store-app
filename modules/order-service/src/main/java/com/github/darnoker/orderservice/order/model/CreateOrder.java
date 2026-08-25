package com.github.darnoker.orderservice.order.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CreateOrder(List<CreateOrderItem> items) {

    public CreateOrder {
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
    }
}
