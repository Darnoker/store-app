package com.github.darnoker.productservice.product.persistence;

import com.github.darnoker.productservice.product.ProductType;
import com.github.darnoker.productservice.product.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    java.util.Optional<Product> findById(UUID id);

    List<Product> findAllByFilters(ProductType type, BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findAllByIdsAndFilters(List<UUID> ids, ProductType type, BigDecimal minPrice, BigDecimal maxPrice);
}
