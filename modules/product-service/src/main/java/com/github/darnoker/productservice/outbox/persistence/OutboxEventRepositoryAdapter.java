package com.github.darnoker.productservice.outbox.persistence;

import com.github.darnoker.productservice.outbox.model.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class OutboxEventRepositoryAdapter implements OutboxEventRepository {
    private final JpaOutboxEventRepository repository;
    public OutboxEvent save(OutboxEvent event) { return OutboxPersistenceMapper.toDomain(repository.save(OutboxPersistenceMapper.toEntity(event))); }
    public int markAsPublished(Collection<UUID> ids, UUID instanceId) { return repository.markAsPublished(ids, instanceId); }
    public List<OutboxEvent> claimBatch(int batchSize, UUID instanceId, Instant leaseUntil) { return repository.claimBatch(batchSize, instanceId, leaseUntil).stream().map(OutboxPersistenceMapper::toDomain).toList(); }
    public int renewLease(Collection<UUID> ids, UUID instanceId, Instant leaseUntil) { return repository.renewLease(ids, instanceId, leaseUntil); }
    public void updateError(UUID id, UUID instanceId, int maxRetries, Instant nextAttemptAt, String error) { repository.updateError(id, instanceId, maxRetries, nextAttemptAt, error); }
}
