package com.github.darnoker.productservice.product;

import com.github.darnoker.productservice.product.model.*;
import com.github.darnoker.productservice.product.persistence.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    private final Clock clock;

    @Transactional
    public Product create(CreateProduct command) {
        log.info("Creating {} product named '{}'", command.productType(), command.name());
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
        Product product = new Product(UUID.randomUUID(), command.name(), command.description(), command.price(), command.productType(), command.details(), now, now);
        Product savedProduct = productRepository.save(product);
        log.info("Created product {}", savedProduct.id());
        return savedProduct;
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Product> findAll(List<UUID> ids, ProductType type, BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            log.warn("Rejected product query with minPrice {} greater than maxPrice {}", minPrice, maxPrice);
            throw new IllegalArgumentException("minPrice must not exceed maxPrice");
        }
        if (ids != null && ids.isEmpty()) {
            return List.of();
        }

        return (ids == null
                ? productRepository.findAllByFilters(type, minPrice, maxPrice)
                : productRepository.findAllByIdsAndFilters(ids, type, minPrice, maxPrice));
    }
}
