package com.github.darnoker.productservice.inventory.model;

import java.util.UUID;

public record ReservedItem(UUID productId, int quantity){
}
