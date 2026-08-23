package com.github.darnoker.orderservice.order;

import com.github.darnoker.orderservice.catalog.ProductCatalog;
import com.github.darnoker.orderservice.order.model.CreateOrder;
import com.github.darnoker.orderservice.order.model.CreateOrderItem;
import com.github.darnoker.orderservice.order.model.Order;
import com.github.darnoker.orderservice.order.model.OrderItem;
import com.github.darnoker.orderservice.order.persistence.OrderMapper;
import com.github.darnoker.orderservice.order.persistence.OrderRepository;
import com.github.darnoker.orderservice.order.persistence.OrderItemRepository;
import com.github.darnoker.orderservice.order.persistence.OrderStatusHistoryRepository;
import com.github.darnoker.orderservice.order.persistence.OrderStatusHistoryEntity;
import lombok.RequiredArgsConstructor;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ProductCatalog productCatalog;
    private final Clock clock;

    @Transactional
    public Order createNewOrder(CreateOrder createOrder) {
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
        Order order = new Order(UUID.randomUUID(), createOrder.customerId(), items, OrderStatus.CREATED, totalAmount, CurrencyCode.PLN, now, now);

        orderRepository.save(OrderMapper.toEntity(order));
        orderItemRepository.saveAll(items.stream()
                .map(item -> OrderMapper.toItemEntity(order.id(), item))
                .toList());
        orderStatusHistoryRepository.save(new OrderStatusHistoryEntity(UUID.randomUUID(), order.id(), OrderStatus.CREATED, now));
        return order;
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(entity -> OrderMapper.toDomain(entity, orderItemRepository.findAllByOrderId(orderId).stream()
                        .map(OrderMapper::toItemDomain).toList()));
    }
}
