package com.github.darnoker.orderservice.order.persistence;

import com.github.darnoker.orderservice.order.model.Order;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
}
