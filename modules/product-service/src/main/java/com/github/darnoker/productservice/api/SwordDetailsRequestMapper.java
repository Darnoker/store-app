package com.github.darnoker.productservice.api;

import com.github.darnoker.productservice.product.model.SwordDetails;

import java.util.Map;

final class SwordDetailsRequestMapper implements ProductDetailsRequestMapper {

    @Override
    public SwordDetails toDomain(Map<String, Object> details) {
        return new SwordDetails(
                integer(details, "damage"),
                decimal(details, "weight"),
                decimal(details, "length"),
                string(details, "material"));
    }
}
