package com.github.darnoker.orderservice.order.persistence;

import com.github.darnoker.orderservice.order.model.Order;
import com.github.darnoker.orderservice.order.model.OrderItem;

import java.util.List;
import java.util.UUID;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderEntity toEntity(Order order) {
        return new OrderEntity(order.id(),
                order.customerId(),
                order.status(),
                order.totalAmount(),
                order.currency(),
                order.createdAt(),
                order.updatedAt());
    }

    public static Order toDomain(OrderEntity entity, List<OrderItem> items) {
        return new Order(entity.getId(),
                entity.getCustomerId(),
                items,
                entity.getStatus(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public static OrderItemEntity toItemEntity(UUID orderId, OrderItem item) {
        return new OrderItemEntity(item.id(), orderId, item.productId(), item.productType(), item.productName(), item.unitPrice(), item.quantity());
    }

    public static OrderItem toItemDomain(OrderItemEntity entity) {
        return new OrderItem(entity.getId(), entity.getProductId(), entity.getProductType(), entity.getProductName(), entity.getUnitPrice(), entity.getQuantity());
    }
}
