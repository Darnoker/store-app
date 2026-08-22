package com.github.darnoker.orderservice.order;

import com.github.darnoker.orderservice.order.model.CreateOrder;
import com.github.darnoker.orderservice.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final Clock clock;

    @Transactional
    public Order createNewOrder(CreateOrder createOrder) {
        Order order = new Order(UUID.randomUUID(), createOrder.customerId(), createOrder.productId(),
                createOrder.quantity(), createOrder.price(), OrderStatus.CREATED, Instant.now(clock));

        orderRepository.save(OrderMapper.mapToOrderEntity(order));
        return order;
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderMapper::mapToOrder);
    }
}
