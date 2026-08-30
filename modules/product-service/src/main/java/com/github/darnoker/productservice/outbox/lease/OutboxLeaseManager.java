package com.github.darnoker.productservice.outbox.lease;

import com.github.darnoker.productservice.outbox.OutboxRelayProperties;
import com.github.darnoker.productservice.outbox.persistence.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxLeaseManager {
    private final OutboxEventRepository repository;
    private final Clock clock;
    private final OutboxRelayProperties properties;
    private final TransactionTemplate transactionTemplate;
    @Qualifier("outboxLeaseTaskScheduler")
    private final TaskScheduler taskScheduler;

    public OutboxLeaseGuard startClaimedBatch(Collection<UUID> messageIds, UUID instanceId) {
        Set<UUID> ids = Set.copyOf(messageIds);
        AtomicBoolean held = new AtomicBoolean(true);
        if (ids.isEmpty()) return new OutboxLeaseGuard(held, null);
        ScheduledFuture<?> heartbeat = taskScheduler.scheduleAtFixedRate(
                () -> renewLease(ids, instanceId, held), properties.leaseRenewalInterval());
        return new OutboxLeaseGuard(held, heartbeat);
    }

    private void renewLease(Set<UUID> ids, UUID instanceId, AtomicBoolean held) {
        try {
            Integer renewed = transactionTemplate.execute(_ -> repository.renewLease(ids, instanceId, Instant.now(clock).plus(properties.leaseDuration())));
            if (renewed == null || renewed != ids.size()) {
                held.set(false);
                log.warn("Lost outbox lease owned by {}", instanceId);
            }
        } catch (RuntimeException exception) {
            held.set(false);
            log.error("Failed to renew outbox lease", exception);
        }
    }
}
