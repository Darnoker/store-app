package com.github.darnoker.orderservice.order.model;

import com.github.darnoker.orderservice.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void acceptsValidOrder() {
        assertDoesNotThrow(() -> new Order(
                UUID.randomUUID(), 1L, 2L, 1, new BigDecimal("10.00"), OrderStatus.CREATED, Instant.now()));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Order(
                UUID.randomUUID(), 1L, 2L, 0, new BigDecimal("10.00"), OrderStatus.CREATED, Instant.now()));
    }

    @Test
    void rejectsNonPositivePrice() {
        assertThrows(IllegalArgumentException.class, () -> new Order(
                UUID.randomUUID(), 1L, 2L, 1, BigDecimal.ZERO, OrderStatus.CREATED, Instant.now()));
    }
}
