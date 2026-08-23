package com.github.darnoker.orderservice.order.model;

import com.github.darnoker.orderservice.order.OrderStatus;
import com.github.darnoker.orderservice.order.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    void acceptsValidOrder() {
        assertDoesNotThrow(() -> new Order(
                UUID.randomUUID(), UUID.randomUUID(), List.of(item(1, new BigDecimal("10.00"))),
                OrderStatus.CREATED, new BigDecimal("10.00"), CurrencyCode.PLN, Instant.now(), Instant.now()));
    }

    @Test
    void rejectsEmptyItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(
                UUID.randomUUID(), UUID.randomUUID(), List.of(), OrderStatus.CREATED,
                new BigDecimal("10.00"), CurrencyCode.PLN, Instant.now(), Instant.now()));
    }

    @Test
    void rejectsNonPositivePrice() {
        assertThrows(IllegalArgumentException.class, () -> item(1, BigDecimal.ZERO));
    }

    private OrderItem item(int quantity, BigDecimal price) {
        return new OrderItem(UUID.randomUUID(), UUID.randomUUID(), "BOOK", "The Witcher", price, quantity);
    }
}
