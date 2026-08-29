package com.github.darnoker.orderservice.outbox.persistence;

import com.github.darnoker.orderservice.outbox.model.OutboxEvent;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {

    OutboxEvent save(OutboxEvent outboxEvent);

    List<OutboxEvent> findPending();

    int markAsPublished(Collection<UUID> uuid, UUID instanceId);

    List<OutboxEvent> claimBatch(int batchSize, UUID instanceId, Instant leaseUntil);

    void updateError(UUID id, UUID instanceId, int retries, Instant nextAttemptAt, String error);
}
