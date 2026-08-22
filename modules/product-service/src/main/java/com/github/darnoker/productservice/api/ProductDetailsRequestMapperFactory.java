package com.github.darnoker.productservice.api;

import com.github.darnoker.productservice.product.ProductType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ProductDetailsRequestMapperFactory {

    static ProductDetailsRequestMapper forProductType(ProductType productType) {
        return switch (productType) {
            case BOOK -> new BookDetailsRequestMapper();
            case SWORD -> new SwordDetailsRequestMapper();
        };
    }
}
