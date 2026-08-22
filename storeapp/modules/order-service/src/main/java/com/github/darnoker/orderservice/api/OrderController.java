package com.github.darnoker.orderservice.api;

import com.github.darnoker.orderservice.generated.api.OrdersApi;
import com.github.darnoker.orderservice.generated.model.CreateOrderRequest;
import com.github.darnoker.orderservice.generated.model.OrderDTO;
import com.github.darnoker.orderservice.order.OrderEntity;
import com.github.darnoker.orderservice.order.OrderService;
import com.github.darnoker.orderservice.order.model.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
public class OrderController implements OrdersApi {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public ResponseEntity<OrderDTO> createOrder(CreateOrderRequest request) {
        Order orderEntity = orderService.createNewOrder(OrderApiMapper.mapToCreateOrder(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orderId}")
                .buildAndExpand(orderEntity.id())
                .toUri();
        return ResponseEntity.created(location).body(toDto(orderEntity));
    }

    @Override
    public ResponseEntity<OrderDTO> getOrder(UUID orderId) {
        return orderService.findById(orderId)
                .map(orderEntity -> ResponseEntity.ok(toDto(orderEntity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private OrderDTO toDto(Order orderEntity) {
        return new OrderDTO()
                .orderId(orderEntity.id())
                .customerId(orderEntity.customerId())
                .productId(orderEntity.productId())
                .quantity(orderEntity.quantity())
                .price(orderEntity.price())
                .status(orderEntity.status().name())
                .createdAt(OffsetDateTime.ofInstant(orderEntity.createdAt(), ZoneOffset.UTC));
    }
}
