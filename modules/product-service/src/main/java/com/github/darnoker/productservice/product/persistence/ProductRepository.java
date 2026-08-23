package com.github.darnoker.productservice.product.persistence;

import com.github.darnoker.productservice.product.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    @Query("""
            SELECT product
            FROM ProductEntity product
            WHERE (:type IS NULL OR product.productType = :type)
              AND (:minPrice IS NULL OR product.price >= :minPrice)
              AND (:maxPrice IS NULL OR product.price <= :maxPrice)
            """)
    List<ProductEntity> findAllByFilters(
            @Param("type") ProductType type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    @Query("""
            SELECT product
            FROM ProductEntity product
            WHERE product.id IN :ids
              AND (:type IS NULL OR product.productType = :type)
              AND (:minPrice IS NULL OR product.price >= :minPrice)
              AND (:maxPrice IS NULL OR product.price <= :maxPrice)
            """)
    List<ProductEntity> findAllByIdsAndFilters(
            @Param("ids") List<UUID> ids,
            @Param("type") ProductType type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}
