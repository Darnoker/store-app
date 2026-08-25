package com.github.darnoker.productservice.inventory.persistence;

import com.github.darnoker.productservice.inventory.StockReservationStatus;
import com.github.darnoker.productservice.inventory.model.StockReservation;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository {

    List<StockReservation> findAllByOrderId(UUID orderId);

    List<StockReservation> findAllByOrderIdAndRequestId(UUID orderId, UUID requestId);

    List<StockReservation> findAllByOrderIdAndStatus(UUID orderId, StockReservationStatus status);

    List<StockReservation> findAllByStatusAndExpiresAtLessThanEqual(StockReservationStatus status, Instant expiresAt);

    Optional<StockReservation> findById(UUID reservationId);

    StockReservation save(StockReservation reservation);

    List<StockReservation> saveAll(Collection<StockReservation> reservations);
}
