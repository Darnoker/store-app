package com.github.darnoker.orderservice.order.model;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductItem(UUID id, String productType, String productName, BigDecimal price, BigDecimal quantity) {
}
