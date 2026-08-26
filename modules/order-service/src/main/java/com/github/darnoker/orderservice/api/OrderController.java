package com.github.darnoker.orderservice.api;

import com.github.darnoker.common.identity.AuthenticatedUser;
import com.github.darnoker.common.identity.CurrentUserProvider;
import com.github.darnoker.orderservice.generated.api.OrdersApi;
import com.github.darnoker.orderservice.generated.model.CreateOrderRequest;
import com.github.darnoker.orderservice.generated.model.CurrencyCode;
import com.github.darnoker.orderservice.generated.model.OrderDTO;
import com.github.darnoker.orderservice.generated.model.OrderItemDTO;
import com.github.darnoker.orderservice.order.OrderService;
import com.github.darnoker.orderservice.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
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

    private final CurrentUserProvider currentUser;

    @Override
    public ResponseEntity<OrderDTO> createOrder(CreateOrderRequest request) {
        return currentUser.currentUser()
                .map(user -> createOrder(request, user))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Override
    public ResponseEntity<OrderDTO> getOrder(UUID orderId) {
        return currentUser.currentUser().map(user -> orderService.findById(orderId)
                        .filter(order -> order.customerId().equals(user.userId()))
                        .map(order -> ResponseEntity.ok(toDto(order)))
                        .orElseGet(() -> ResponseEntity.notFound().build()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Override
    public ResponseEntity<java.util.List<OrderDTO>> getMyOrders() {
        return currentUser.currentUser()
                .map(user -> ResponseEntity.ok(orderService.findByCustomerId(user.userId()).stream().map(this::toDto).toList()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
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

    private @NonNull ResponseEntity<OrderDTO> createOrder(CreateOrderRequest request, AuthenticatedUser user) {
        Order order = orderService.createNewOrder(user.userId(), OrderApiMapper.mapToCreateOrder(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orderId}")
                .buildAndExpand(order.id())
                .toUri();
        return ResponseEntity.created(location)
                .body(toDto(order));
    }
}
