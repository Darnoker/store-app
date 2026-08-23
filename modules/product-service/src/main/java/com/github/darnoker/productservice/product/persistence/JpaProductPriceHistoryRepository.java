package com.github.darnoker.productservice.product.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface JpaProductPriceHistoryRepository extends JpaRepository<ProductPriceHistoryEntity, UUID> {

    List<ProductPriceHistoryEntity> findAllByProductIdOrderByValidFromAsc(UUID productId);
}
