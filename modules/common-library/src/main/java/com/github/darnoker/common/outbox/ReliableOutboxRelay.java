package com.github.darnoker.common.outbox;

import com.github.darnoker.common.async.AsyncMessagePublisher;
import com.github.darnoker.common.async.OutboundMessage;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ReliableOutboxRelay {
    private final AsyncMessagePublisher publisher;
    private final OutboxRelayRepository repository;
    private final Clock clock;
    private final TransactionRunner transactionRunner;
    private final OutboxRelaySettings settings;
    private final RecurringTaskScheduler scheduler;
    private final UUID instanceId = UUID.randomUUID();

    public ReliableOutboxRelay(AsyncMessagePublisher publisher, OutboxRelayRepository repository, Clock clock, TransactionRunner transactionRunner, OutboxRelaySettings settings, RecurringTaskScheduler scheduler) {
        this.publisher = publisher;
        this.repository = repository;
        this.clock = clock;
        this.transactionRunner = transactionRunner;
        this.settings = settings;
        this.scheduler = scheduler;
    }

    public void relay() {
        List<OutboundMessage> messages = transactionRunner.execute(() -> repository.claimBatch(settings.batchSize(), instanceId, Instant.now(clock).plus(settings.leaseDuration())));
        if (messages == null || messages.isEmpty()) return;
        var held = new java.util.concurrent.atomic.AtomicBoolean(true);
        var heartbeat = scheduler.scheduleAtFixedRate(() -> renew(messages, held), settings.leaseRenewalInterval());
        try {
            for (OutboundMessage message : messages) {
                if (!held.get()) break;
                publish(message);
            }
        } finally {
            heartbeat.cancel();
        }
    }

    private void renew(List<OutboundMessage> messages, java.util.concurrent.atomic.AtomicBoolean held) {
        Integer renewed = transactionRunner.execute(() -> repository.renewLease(messages.stream().map(OutboundMessage::id).toList(), instanceId, Instant.now(clock).plus(settings.leaseDuration())));
        if (renewed == null || renewed != messages.size()) held.set(false);
    }

    private void publish(OutboundMessage message) {
        try {
            publisher.publish(message).toCompletableFuture().get(settings.publishTimeout().toMillis(), TimeUnit.MILLISECONDS);
            transactionRunner.executeWithoutResult(() -> repository.markAsPublished(List.of(message.id()), instanceId));
        } catch (Exception exception) {
            transactionRunner.executeWithoutResult(() -> repository.updateError(message.id(), instanceId, settings.maxRetries(), Instant.now(clock).plusSeconds(60), error(exception)));
        }
    }

    private String error(Exception exception) {
        String message = exception.getMessage() == null ? exception.getClass().getName() : exception.getMessage();
        return message.substring(0, Math.min(message.length(), 2000));
    }
}
