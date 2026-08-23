package com.github.darnoker.productservice.inventory.persistence;

import com.github.darnoker.productservice.inventory.model.Inventory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    List<Inventory> findAllByProductIdInForUpdate(Collection<UUID> productIds);

    Optional<Inventory> findByProductIdForUpdate(UUID productId);

    Optional<Inventory> findById(UUID productId);

    Inventory save(Inventory inventory);

    List<Inventory> saveAll(Collection<Inventory> inventories);
}
