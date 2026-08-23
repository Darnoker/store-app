package com.github.darnoker.productservice.inventory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {

    Collection<InventoryEntity> findAllByProductIdIn(Collection<UUID> productIds);
}
