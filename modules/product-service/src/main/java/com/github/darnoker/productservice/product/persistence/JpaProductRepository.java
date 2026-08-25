package com.github.darnoker.productservice.product.persistence;

import com.github.darnoker.productservice.product.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

    @Query("select product from ProductEntity product where (:type is null or product.productType = :type) and (:minPrice is null or product.price >= :minPrice) and (:maxPrice is null or product.price <= :maxPrice)")
    List<ProductEntity> findAllByFilters(@Param("type") ProductType type, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("select product from ProductEntity product where product.id in :ids and (:type is null or product.productType = :type) and (:minPrice is null or product.price >= :minPrice) and (:maxPrice is null or product.price <= :maxPrice)")
    List<ProductEntity> findAllByIdsAndFilters(@Param("ids") List<UUID> ids, @Param("type") ProductType type, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
}
