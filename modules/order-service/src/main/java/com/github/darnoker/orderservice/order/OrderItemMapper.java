package com.github.darnoker.orderservice.order;

import com.github.darnoker.orderservice.order.model.OrderItem;
import com.github.darnoker.orderservice.order.model.ProductItem;

import java.math.BigDecimal;

public class OrderItemMapper {

    public static ProductItem toProductItem(OrderItem orderItem) {
        return new ProductItem(
                orderItem.productId(),
                orderItem.productType(),
                orderItem.productName(),
                orderItem.unitPrice(),
                BigDecimal.valueOf(orderItem.quantity())
        );
    }
}
