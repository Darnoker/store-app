package com.github.darnoker.productservice.product.persistence;

import com.github.darnoker.productservice.product.ProductType;
import com.github.darnoker.productservice.product.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository productRepository;

    private final ProductMapper productMapper;

    @Override
    public Product save(Product product) {
        return productMapper.toDomain(productRepository.save(productMapper.toEntity(product)));
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id).map(productMapper::toDomain);
    }

    @Override
    public List<Product> findAllByFilters(ProductType type, BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findAllByFilters(type, minPrice, maxPrice).stream().map(productMapper::toDomain).toList();
    }

    @Override
    public List<Product> findAllByIdsAndFilters(List<UUID> ids, ProductType type, BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findAllByIdsAndFilters(ids, type, minPrice, maxPrice).stream().map(productMapper::toDomain).toList();
    }
}
