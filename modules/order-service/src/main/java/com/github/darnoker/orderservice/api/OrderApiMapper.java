package com.github.darnoker.orderservice.api;

import com.github.darnoker.orderservice.generated.model.CreateOrderRequest;
import com.github.darnoker.orderservice.order.model.CreateOrder;
import com.github.darnoker.orderservice.order.model.CreateOrderItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OrderApiMapper {

    static CreateOrder mapToCreateOrder(CreateOrderRequest request) {
        return new CreateOrder(request.getCustomerId(), request.getItems().stream()
                .map(item -> new CreateOrderItem(item.getProductId(), item.getQuantity()))
                .toList());
    }

}
