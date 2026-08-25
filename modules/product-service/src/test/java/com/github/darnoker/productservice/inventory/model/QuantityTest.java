package com.github.darnoker.productservice.inventory.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuantityTest {

    @Test
    void rejectsNegativeValuesAndSubtractions() {
        assertThrows(IllegalArgumentException.class, () -> new Quantity(-1));
        assertThrows(IllegalArgumentException.class, () -> new Quantity(1).subtract(new Quantity(2)));
    }

    @Test
    void addsAndComparesQuantities() {
        Quantity quantity = new Quantity(3).add(new Quantity(2));

        assertEquals(5, quantity.value());
        assertEquals(true, new Quantity(3).isLessThan(quantity));
    }

    @Test
    void preventsReservedQuantityFromExceedingInventoryQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Inventory(
                UUID.randomUUID(), new Quantity(2), new Quantity(3), Instant.now()));
    }
}
