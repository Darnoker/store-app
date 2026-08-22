package com.github.darnoker.productservice.api;

import com.github.darnoker.productservice.product.model.ProductDetails;

import java.util.Map;

interface ProductDetailsRequestMapper {

    ProductDetails toDomain(Map<String, Object> details);

    default String string(Map<String, Object> details, String field) {
        Object value = required(details, field);
        if (value instanceof String string) {
            return string;
        }
        throw invalidType(field, "a string", value);
    }

    default Integer integer(Map<String, Object> details, String field) {
        Object value = required(details, field);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw invalidType(field, "a number", value);
    }

    default Double decimal(Map<String, Object> details, String field) {
        Object value = required(details, field);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw invalidType(field, "a number", value);
    }

    private static Object required(Map<String, Object> details, String field) {
        Object value = details.get(field);
        if (value == null) {
            throw new IllegalArgumentException("Product details field '%s' is required".formatted(field));
        }
        return value;
    }

    private static IllegalArgumentException invalidType(String field, String expectedType, Object value) {
        return new IllegalArgumentException("Product details field '%s' must be %s, but was %s"
                .formatted(field, expectedType, value.getClass().getSimpleName()));
    }
}
