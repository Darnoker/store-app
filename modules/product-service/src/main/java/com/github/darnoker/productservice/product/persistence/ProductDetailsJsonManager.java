package com.github.darnoker.productservice.product.persistence;

import com.github.darnoker.productservice.product.ProductType;
import com.github.darnoker.productservice.product.model.BookDetails;
import com.github.darnoker.productservice.product.model.ProductDetails;
import com.github.darnoker.productservice.product.model.SwordDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
class ProductDetailsJsonManager {

    private final JsonMapper jsonMapper;

    public String toJson(ProductDetails details) {
        try {
            return jsonMapper.writeValueAsString(details);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize product details", exception);
        }
    }

    public ProductDetails fromJson(ProductType productType, String details) {
        try {
            return switch (productType) {
                case BOOK -> jsonMapper.readValue(details, BookDetails.class);
                case SWORD -> jsonMapper.readValue(details, SwordDetails.class);
            };
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not deserialize product details", exception);
        }
    }
}
