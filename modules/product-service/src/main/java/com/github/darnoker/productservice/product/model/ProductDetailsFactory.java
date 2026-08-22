package com.github.darnoker.productservice.product.model;

import com.github.darnoker.productservice.product.ProductType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductDetailsFactory {

    public static ProductDetails create(ProductType productType, Map<String, Object> details) {
        return switch (productType) {
            case BOOK -> new BookDetails(
                    string(details, "isbn"),
                    integer(details, "pages"),
                    string(details, "author"),
                    string(details, "publisher"),
                    string(details, "language"));
            case SWORD -> new SwordDetails(
                    integer(details, "damage"),
                    decimal(details, "weight"),
                    decimal(details, "length"),
                    string(details, "material"));
        };
    }

    private static String string(Map<String, Object> details, String field) {
        return (String) details.get(field);
    }

    private static Integer integer(Map<String, Object> details, String field) {
        return ((Number) details.get(field)).intValue();
    }

    private static Double decimal(Map<String, Object> details, String field) {
        return ((Number) details.get(field)).doubleValue();
    }
}
