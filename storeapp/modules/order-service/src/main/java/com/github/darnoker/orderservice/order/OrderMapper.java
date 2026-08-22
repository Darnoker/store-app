package com.github.darnoker.orderservice.order;

import com.github.darnoker.orderservice.order.model.Order;

public class OrderMapper {

    public static OrderEntity mapToOrderEntity(Order order) {
        return new OrderEntity(order.id(),
                order.customerId(),
                order.productId(),
                order.quantity(),
                order.price(),
                order.status(),
                order.createdAt());
    }

    public static Order mapToOrder(OrderEntity orderEntity) {
        return new Order(orderEntity.getId(),
                orderEntity.getCustomerId(),
                orderEntity.getProductId(),
                orderEntity.getQuantity(),
                orderEntity.getPrice(),
                orderEntity.getStatus(),
                orderEntity.getCreatedAt());
    }
}
