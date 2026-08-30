package com.github.darnoker.common.outbox;

import com.github.darnoker.common.async.OutboundMessage;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxRelayRepository {
    List<OutboundMessage> claimBatch(int batchSize, UUID instanceId, Instant leaseUntil);
    int markAsPublished(Collection<UUID> ids, UUID instanceId);
    int renewLease(Collection<UUID> ids, UUID instanceId, Instant leaseUntil);
    void updateError(UUID id, UUID instanceId, int maxRetries, Instant nextAttemptAt, String error);
}
