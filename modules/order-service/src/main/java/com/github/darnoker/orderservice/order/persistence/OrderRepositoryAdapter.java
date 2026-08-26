package com.github.darnoker.orderservice.order.persistence;

import com.github.darnoker.orderservice.order.OrderStatus;
import com.github.darnoker.orderservice.order.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class OrderRepositoryAdapter implements OrderRepository {
    private final JpaOrderRepository orderRepository;
    private final JpaOrderItemRepository orderItemRepository;
    private final JpaOrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    public Order save(Order order) {
        orderRepository.save(OrderMapper.toEntity(order));
        orderItemRepository.saveAll(order.items().stream().map(item -> OrderMapper.toItemEntity(order.id(), item)).toList());
        orderStatusHistoryRepository.save(new OrderStatusHistoryEntity(UUID.randomUUID(), order.id(), OrderStatus.CREATED, order.createdAt()));
        return order;
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(entity -> OrderMapper.toDomain(entity, orderItemRepository.findAllByOrderId(orderId).stream()
                        .map(OrderMapper::toItemDomain)
                        .toList()));
    }

    @Override
    public java.util.List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(entity -> OrderMapper.toDomain(entity, orderItemRepository.findAllByOrderId(entity.getId()).stream()
                        .map(OrderMapper::toItemDomain).toList()))
                .toList();
    }
}
