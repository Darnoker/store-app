package com.github.darnoker.productservice.inventory.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@NoArgsConstructor
@AllArgsConstructor
class InventoryEntity {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
