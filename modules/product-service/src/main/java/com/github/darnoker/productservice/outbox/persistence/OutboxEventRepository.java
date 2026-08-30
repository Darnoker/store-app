package com.github.darnoker.productservice.outbox.persistence;

import com.github.darnoker.productservice.outbox.model.OutboxEvent;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {
    OutboxEvent save(OutboxEvent event);
    int markAsPublished(Collection<UUID> ids, UUID instanceId);
    List<OutboxEvent> claimBatch(int batchSize, UUID instanceId, Instant leaseUntil);
    int renewLease(Collection<UUID> ids, UUID instanceId, Instant leaseUntil);
    void updateError(UUID id, UUID instanceId, int maxRetries, Instant nextAttemptAt, String error);
}
