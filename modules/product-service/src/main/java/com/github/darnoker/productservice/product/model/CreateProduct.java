package com.github.darnoker.productservice.product.model;

import com.github.darnoker.productservice.product.ProductType;

import java.math.BigDecimal;

public record CreateProduct(String name, String description, BigDecimal price, ProductType productType,
                            ProductDetails details) {
}
