package com.github.darnoker.productservice.product.model;

import com.github.darnoker.productservice.product.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Product(UUID id, String name, String description, BigDecimal price, ProductType productType,
                      ProductDetails details, Instant createdAt, Instant updatedAt) {
}
