package com.github.darnoker.orderservice.order.model;

import java.math.BigDecimal;

public record CreateOrder(Long customerId, Long productId, Integer quantity, BigDecimal price) {
}
