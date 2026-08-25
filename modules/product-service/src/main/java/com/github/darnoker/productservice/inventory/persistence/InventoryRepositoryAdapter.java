package com.github.darnoker.productservice.inventory.persistence;

import com.github.darnoker.productservice.inventory.model.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class InventoryRepositoryAdapter implements InventoryRepository {

    private final JpaInventoryRepository inventoryRepository;

    @Override
    public List<Inventory> findAllByProductIdInForUpdate(Collection<UUID> productIds) {
        return inventoryRepository.findAllByProductIdInForUpdate(productIds).stream()
                .map(InventoryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Inventory> findByProductIdForUpdate(UUID productId) {
        return inventoryRepository.findByProductIdForUpdate(productId)
                .map(InventoryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Inventory> findById(UUID productId) {
        return inventoryRepository.findById(productId)
                .map(InventoryPersistenceMapper::toDomain);
    }

    @Override
    public Inventory save(Inventory inventory) {
        return InventoryPersistenceMapper.toDomain(inventoryRepository.save(InventoryPersistenceMapper.toEntity(inventory)));
    }

    @Override
    public List<Inventory> saveAll(Collection<Inventory> inventories) {
        return inventoryRepository.saveAll(inventories.stream().map(InventoryPersistenceMapper::toEntity).toList()).stream()
                .map(InventoryPersistenceMapper::toDomain)
                .toList();
    }
}
