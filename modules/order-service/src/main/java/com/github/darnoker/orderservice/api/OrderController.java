package com.github.darnoker.orderservice.api;

import com.github.darnoker.orderservice.generated.api.OrdersApi;
import com.github.darnoker.orderservice.generated.model.CreateOrderRequest;
import com.github.darnoker.orderservice.generated.model.CurrencyCode;
import com.github.darnoker.orderservice.generated.model.OrderDTO;
import com.github.darnoker.orderservice.generated.model.OrderItemDTO;
import com.github.darnoker.orderservice.order.OrderService;
import com.github.darnoker.orderservice.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<OrderDTO> createOrder(CreateOrderRequest request) {
        Order order = orderService.createNewOrder(OrderApiMapper.mapToCreateOrder(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orderId}")
                .buildAndExpand(order.id())
                .toUri();

        return ResponseEntity.created(location).body(toDto(order));
    }

    @Override
    public ResponseEntity<OrderDTO> getOrder(UUID orderId) {
        return orderService.findById(orderId)
                .map(order -> ResponseEntity.ok(toDto(order)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private OrderDTO toDto(Order order) {
        return new OrderDTO()
                .orderId(order.id())
                .customerId(order.customerId())
                .items(order.items().stream().map(item -> new OrderItemDTO()
                        .orderItemId(item.id()).productId(item.productId()).productType(item.productType()).productName(item.productName())
                        .unitPrice(item.unitPrice()).quantity(item.quantity())).toList())
                .totalAmount(order.totalAmount())
                .currency(CurrencyCode.fromValue(order.currency().name()))
                .status(order.status().name())
                .createdAt(OffsetDateTime.ofInstant(order.createdAt(), ZoneOffset.UTC))
                .updatedAt(OffsetDateTime.ofInstant(order.updatedAt(), ZoneOffset.UTC));
    }
}
