package com.github.darnoker.orderservice.order.event;

import com.github.darnoker.orderservice.order.CurrencyCode;
import com.github.darnoker.orderservice.order.model.ProductItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreated(UUID orderId, UUID customerId, CurrencyCode currencyCode, Instant createdAt, List<ProductItem> items)
        implements OrderEvent {
}
