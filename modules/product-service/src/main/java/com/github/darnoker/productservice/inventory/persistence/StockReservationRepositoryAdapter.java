package com.github.darnoker.productservice.inventory.persistence;

import com.github.darnoker.productservice.inventory.StockReservationStatus;
import com.github.darnoker.productservice.inventory.model.StockReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class StockReservationRepositoryAdapter implements StockReservationRepository {

    private final JpaStockReservationRepository stockReservationRepository;

    @Override
    public List<StockReservation> findAllByOrderId(UUID orderId) {
        return stockReservationRepository.findAllByOrderId(orderId).stream()
                .map(StockReservationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockReservation> findAllByOrderIdAndRequestId(UUID orderId, UUID requestId) {
        return stockReservationRepository.findAllByOrderIdAndRequestId(orderId, requestId).stream()
                .map(StockReservationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockReservation> findAllByOrderIdAndStatus(UUID orderId, StockReservationStatus status) {
        return stockReservationRepository.findAllByOrderIdAndStatus(orderId, status).stream()
                .map(StockReservationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockReservation> findAllByStatusAndExpiresAtLessThanEqual(StockReservationStatus status, Instant expiresAt) {
        return stockReservationRepository.findAllByStatusAndExpiresAtLessThanEqual(status, expiresAt).stream()
                .map(StockReservationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<StockReservation> findById(UUID reservationId) {
        return stockReservationRepository.findById(reservationId).map(StockReservationPersistenceMapper::toDomain);
    }

    @Override
    public StockReservation save(StockReservation reservation) {
        return StockReservationPersistenceMapper.toDomain(
                stockReservationRepository.save(StockReservationPersistenceMapper.toEntity(reservation)));
    }

    @Override
    public List<StockReservation> saveAll(Collection<StockReservation> reservations) {
        return stockReservationRepository.saveAll(reservations.stream().map(StockReservationPersistenceMapper::toEntity).toList()).stream()
                .map(StockReservationPersistenceMapper::toDomain)
                .toList();
    }
}
