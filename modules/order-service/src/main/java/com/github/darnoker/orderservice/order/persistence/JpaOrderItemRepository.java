package com.github.darnoker.orderservice.order.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface JpaOrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {

    List<OrderItemEntity> findAllByOrderId(UUID orderId);
}
