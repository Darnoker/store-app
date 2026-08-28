package com.github.darnoker.orderservice.order;

import com.github.darnoker.orderservice.catalog.ProductCatalog;
import com.github.darnoker.orderservice.order.event.OrderCreated;
import com.github.darnoker.orderservice.order.model.CreateOrder;
import com.github.darnoker.orderservice.order.model.CreateOrderItem;
import com.github.darnoker.orderservice.order.model.Order;
import com.github.darnoker.orderservice.order.persistence.OrderRepository;
import com.github.darnoker.orderservice.outbox.EventType;
import com.github.darnoker.orderservice.outbox.OutboxService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Test
    void storesOrderCreatedEventForTheOrderTopic() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        ProductCatalog productCatalog = mock(ProductCatalog.class);
        OutboxService outboxService = mock(OutboxService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-28T19:00:00Z"), ZoneOffset.UTC);
        OrderService orderService = new OrderService(orderRepository, productCatalog, outboxService, clock);
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(productCatalog.getProducts(List.of(productId)))
                .thenReturn(List.of(new ProductCatalog.ProductSnapshot(productId, "Book", "BOOK", new BigDecimal("12.00"))));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createNewOrder(customerId, new CreateOrder(List.of(new CreateOrderItem(productId, 2))));

        ArgumentCaptor<OrderCreated> eventCaptor = ArgumentCaptor.forClass(OrderCreated.class);
        verify(outboxService).save(eq(order.id()), eq(OrderTopics.ORDER_TOPIC), eq(EventType.ORDER_CREATED), eventCaptor.capture());
        assertEquals(order.id(), eventCaptor.getValue().orderId());
        assertEquals(customerId, eventCaptor.getValue().customerId());
        assertEquals(Instant.now(clock), eventCaptor.getValue().createdAt());
    }
}
