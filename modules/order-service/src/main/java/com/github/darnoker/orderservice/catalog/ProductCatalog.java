package com.github.darnoker.orderservice.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductCatalog {

    List<ProductSnapshot> getProducts(List<UUID> uuids);

    record ProductSnapshot(UUID id, String name, String type, BigDecimal price) {
    }
}
