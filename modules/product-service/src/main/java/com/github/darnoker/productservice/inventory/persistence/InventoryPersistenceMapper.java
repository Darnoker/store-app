package com.github.darnoker.productservice.inventory.persistence;

import com.github.darnoker.productservice.inventory.model.Inventory;
import com.github.darnoker.productservice.inventory.model.Quantity;

final class InventoryPersistenceMapper {

    private InventoryPersistenceMapper() {
    }

    static Inventory toDomain(InventoryEntity entity) {
        return new Inventory(
                entity.getProductId(),
                new Quantity(entity.getQuantity()),
                new Quantity(entity.getReservedQuantity()),
                entity.getUpdatedAt());
    }

    static InventoryEntity toEntity(Inventory inventory) {
        return new InventoryEntity(
                inventory.productId(),
                inventory.quantity().value(),
                inventory.reservedQuantity().value(),
                inventory.updatedAt());
    }
}
