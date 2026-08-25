package com.github.darnoker.productservice.inventory.model;

import java.util.UUID;

public record ReservedItem(UUID productId, Quantity quantity) {

    public ReservedItem(UUID productId, int quantity) {
        this(productId, new Quantity(quantity));
    }
}
