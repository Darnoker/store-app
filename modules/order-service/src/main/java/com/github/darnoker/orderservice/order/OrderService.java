package com.github.darnoker.orderservice.order;

import com.github.darnoker.orderservice.catalog.ProductCatalog;
import com.github.darnoker.orderservice.order.event.OrderCreated;
import com.github.darnoker.orderservice.order.model.CreateOrder;
import com.github.darnoker.orderservice.order.model.CreateOrderItem;
import com.github.darnoker.orderservice.order.model.Order;
import com.github.darnoker.orderservice.order.model.OrderItem;
import com.github.darnoker.orderservice.order.persistence.OrderRepository;
import com.github.darnoker.orderservice.outbox.EventType;
import com.github.darnoker.orderservice.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductCatalog productCatalog;
    private final OutboxService outboxService;
    private final Clock clock;

    @Transactional
    public Order createNewOrder(UUID customerId, CreateOrder createOrder) {
        log.info("Creating order for customer {} with {} item(s)", customerId, createOrder.items().size());
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
        Map<UUID, ProductCatalog.ProductSnapshot> productsById = productCatalog.getProducts(
                        createOrder.items().stream()
                                .map(CreateOrderItem::productId)
                                .distinct()
                                .toList()).stream()
                .collect(Collectors.toMap(ProductCatalog.ProductSnapshot::id, Function.identity()));
        List<OrderItem> items = createOrder.items().stream()
                .map(item -> {
                    ProductCatalog.ProductSnapshot product = productsById.get(item.productId());
                    return new OrderItem(UUID.randomUUID(), product.id(), product.type(), product.name(), product.price(), item.quantity());
                })
                .toList();
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = new Order(UUID.randomUUID(), customerId, items, OrderStatus.CREATED, totalAmount, CurrencyCode.PLN, now, now);

        Order savedOrder = orderRepository.save(order);
        saveOrderCreatedEvent(customerId, savedOrder, items);

        log.info("Created order {} for customer {}", savedOrder.id(), customerId);
        return savedOrder;
    }

    private void saveOrderCreatedEvent(UUID customerId, Order savedOrder, List<OrderItem> items) {
        outboxService.save(savedOrder.id(), OrderTopics.ORDER_TOPIC, EventType.ORDER_CREATED, new OrderCreated(
                        savedOrder.id(),
                        customerId,
                        CurrencyCode.PLN,
                        Instant.now(clock),
                        items.stream()
                                .map(OrderItemMapper::toProductItem)
                                .toList()
                )
        );
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        log.debug("Order {} {}", orderId, order.isPresent() ? "found" : "not found");
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(UUID customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

}
