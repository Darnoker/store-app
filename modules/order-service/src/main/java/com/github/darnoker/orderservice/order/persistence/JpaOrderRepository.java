package com.github.darnoker.orderservice.order.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
