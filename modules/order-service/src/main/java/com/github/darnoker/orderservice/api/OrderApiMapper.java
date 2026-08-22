package com.github.darnoker.orderservice.api;

import com.github.darnoker.orderservice.generated.model.CreateOrderRequest;
import com.github.darnoker.orderservice.order.model.CreateOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OrderApiMapper {

    static CreateOrder mapToCreateOrder(CreateOrderRequest request) {
        return new CreateOrder(request.getCustomerId(), request.getProductId(), request.getQuantity(), request.getPrice());
    }

}
