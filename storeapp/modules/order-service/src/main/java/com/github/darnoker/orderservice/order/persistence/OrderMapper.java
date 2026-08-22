package com.github.darnoker.orderservice.order.persistence;

import com.github.darnoker.orderservice.order.model.Order;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderEntity toEntity(Order order) {
        return new OrderEntity(order.id(),
                order.customerId(),
                order.productId(),
                order.quantity(),
                order.price(),
                order.status(),
                order.createdAt());
    }

    public static Order toDomain(OrderEntity entity) {
        return new Order(entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getPrice(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
