package com.github.darnoker.productservice.inventory.persistence;

import com.github.darnoker.productservice.inventory.StockReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservationEntity, UUID> {

    List<StockReservationEntity> findAllByOrderId(UUID orderId);

    List<StockReservationEntity> findAllByOrderIdAndRequestId(UUID orderId, UUID requestId);

    List<StockReservationEntity> findAllByOrderIdAndStatus(UUID orderId, StockReservationStatus status);

    List<StockReservationEntity> findAllByStatusAndExpiresAtLessThanEqual(StockReservationStatus status, Instant expiresAt);
}
