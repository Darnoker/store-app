package com.github.darnoker.productservice.product.model;

public record SwordDetails(Integer damage, Double weight, Double length, String material) implements ProductDetails {
}
