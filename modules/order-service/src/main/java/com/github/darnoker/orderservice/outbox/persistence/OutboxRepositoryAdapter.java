package com.github.darnoker.orderservice.outbox.persistence;

import com.github.darnoker.orderservice.outbox.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class OutboxRepositoryAdapter implements OutboxRepository {

    private final JpaOutboxEventRepository repository;

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return OutboxPersistenceMapper.toDomain(repository.save(OutboxPersistenceMapper.toEntity(outboxEvent)));
    }

    @Override
    public List<OutboxEvent> findPending() {
        return repository.findAllByStatus(OutboxEventEntityStatus.PENDING).stream()
                .map(OutboxPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public int markAsPublished(Collection<UUID> uuid, UUID instanceId) {
        return repository.markAsPublished(uuid, instanceId);
    }

    @Override
    public List<OutboxEvent> claimBatch(int batchSize, UUID instanceId, Instant leaseUntil) {
        return repository.claimBatch(batchSize, instanceId, leaseUntil).stream()
                .map(OutboxPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public int renewLease(Collection<UUID> ids, UUID instanceId, Instant leaseUntil) {
        return repository.renewLease(ids, instanceId, leaseUntil);
    }

    @Override
    public void updateError(UUID id, UUID instanceId, int retries, Instant nextAttemptAt, String error) {
        repository.updateError(id, instanceId, retries, nextAttemptAt, error);
    }
}
