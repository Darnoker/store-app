package com.github.darnoker.productservice.api;

import com.github.darnoker.productservice.generated.model.CreateProductRequest;
import com.github.darnoker.productservice.product.ProductType;
import com.github.darnoker.productservice.product.model.CreateProduct;
import com.github.darnoker.productservice.product.model.ProductDetails;
import com.github.darnoker.productservice.product.model.ProductDetailsFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ProductApiMapper {

    static CreateProduct toCommand(CreateProductRequest request) {
        ProductType productType = ProductType.valueOf(request.getProductType().getValue());
        ProductDetails details = ProductDetailsFactory.create(productType, request.getDetails());
        return new CreateProduct(request.getName(), request.getDescription(), request.getPrice(), productType, details);
    }
}
