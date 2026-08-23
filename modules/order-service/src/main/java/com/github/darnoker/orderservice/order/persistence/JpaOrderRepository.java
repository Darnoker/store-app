package com.github.darnoker.orderservice.order.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> { }
