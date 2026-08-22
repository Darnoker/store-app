package com.github.darnoker.productservice.product.persistence;

import com.github.darnoker.productservice.product.model.Product;
import com.github.darnoker.productservice.product.model.ProductDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductDetailsJsonManager productDetailsJsonManager;

    public ProductEntity toEntity(Product product) {
        return new ProductEntity(product.id(), product.name(), product.description(), product.price(), product.productType(),
                productDetailsJsonManager.toJson(product.details()), product.createdAt(), product.updatedAt());
    }

    public Product toDomain(ProductEntity entity) {
        ProductDetails details = productDetailsJsonManager.fromJson(entity.getProductType(), entity.getDetails());
        return new Product(entity.getId(), entity.getName(), entity.getDescription(), entity.getPrice(), entity.getProductType(), details,
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
