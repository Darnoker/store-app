package com.github.darnoker.orderservice.outbox.lease;

import com.github.darnoker.orderservice.outbox.OutboxRelayProperties;
import com.github.darnoker.orderservice.outbox.persistence.OutboxRepository;
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
@Slf4j
public class OutboxLeaseManager {

    private final OutboxRepository repository;
    private final Clock clock;
    private final OutboxRelayProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final TaskScheduler taskScheduler;

    public OutboxLeaseManager(
            OutboxRepository repository,
            Clock clock,
            OutboxRelayProperties properties,
            TransactionTemplate transactionTemplate,
            @Qualifier("outboxLeaseTaskScheduler") TaskScheduler taskScheduler
    ) {
        this.repository = repository;
        this.clock = clock;
        this.properties = properties;
        this.transactionTemplate = transactionTemplate;
        this.taskScheduler = taskScheduler;
    }

    public OutboxLeaseGuard startClaimedBatch(Collection<UUID> messageIds, UUID instanceId) {
        Set<UUID> ids = Set.copyOf(messageIds);
        AtomicBoolean held = new AtomicBoolean(true);

        if (ids.isEmpty()) {
            return new OutboxLeaseGuard(held, null);
        }

        ScheduledFuture<?> heartbeat = taskScheduler.scheduleAtFixedRate(
                () -> renewLease(ids, instanceId, held),
                properties.leaseRenewalInterval()
        );
        return new OutboxLeaseGuard(held, heartbeat);
    }

    private void renewLease(Set<UUID> messageIds, UUID instanceId, AtomicBoolean held) {
        if (!held.get()) {
            return;
        }

        try {
            Integer renewed = transactionTemplate.execute(_ -> repository.renewLease(
                    messageIds,
                    instanceId,
                    Instant.now(clock).plus(properties.leaseDuration())
            ));
            if (renewed == null || renewed != messageIds.size()) {
                held.set(false);
                log.warn("Lost the lease for outbox batch owned by {}: renewed {} of {} messages",
                        instanceId, renewed, messageIds.size());
            }
        } catch (RuntimeException exception) {
            held.set(false);
            log.error("Failed to renew the lease for outbox batch owned by {}", instanceId, exception);
        }
    }
}
