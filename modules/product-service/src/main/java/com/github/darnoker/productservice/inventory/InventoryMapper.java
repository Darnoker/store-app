package com.github.darnoker.productservice.inventory;


import com.github.darnoker.productservice.inventory.model.Inventory;
import com.github.darnoker.productservice.inventory.persistence.InventoryEntity;

public final class InventoryMapper {

    private InventoryMapper() {

    }

    public static Inventory toDomain(InventoryEntity entity) {
        return new Inventory(entity.getProductId(), entity.getQuantity(), entity.getReservedQuantity(), entity.getUpdatedAt());
    }

    public static InventoryEntity toPersistence(Inventory inventory) {
        return new InventoryEntity(inventory.productId(), inventory.quantity(), inventory.reservedQuantity(), inventory.updatedAt());
    }
}
