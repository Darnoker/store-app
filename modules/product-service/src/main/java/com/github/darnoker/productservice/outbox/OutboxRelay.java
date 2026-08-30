package com.github.darnoker.productservice.outbox;

import com.github.darnoker.common.async.AsyncMessagePublisher;
import com.github.darnoker.common.async.OutboundMessage;
import com.github.darnoker.productservice.outbox.model.OutboxEvent;
import com.github.darnoker.productservice.outbox.lease.OutboxLeaseGuard;
import com.github.darnoker.productservice.outbox.lease.OutboxLeaseManager;
import com.github.darnoker.productservice.outbox.persistence.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {
    private static final UUID INSTANCE_ID = UUID.randomUUID();
    private static final int MAX_RETRIES = 10;
    private final AsyncMessagePublisher publisher;
    private final OutboxEventRepository repository;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;
    private final OutboxRelayProperties properties;
    private final OutboxLeaseManager leaseManager;

    @Scheduled(fixedDelayString = "${outbox.relay.polling-interval-ms:10000}")
    public void relay() {
        List<OutboxEvent> events = transactionTemplate.execute(_ -> repository.claimBatch(properties.batchSize(), INSTANCE_ID, Instant.now(clock).plus(properties.leaseDuration())));
        if (events == null || events.isEmpty()) return;
        try (OutboxLeaseGuard leaseGuard = leaseManager.startClaimedBatch(
                events.stream().map(OutboxEvent::id).collect(java.util.stream.Collectors.toSet()), INSTANCE_ID)) {
            for (OutboxEvent event : events) {
                if (!leaseGuard.isHeld()) {
                    log.warn("Stopping outbox relay because its lease was lost");
                    break;
                }
                try {
                    publisher.publish(new OutboundMessage(event.id(), event.destination(), event.aggregateId(), event.eventType(), event.createdAt(), event.payload()))
                            .toCompletableFuture().get(properties.publishTimeout().toMillis(), TimeUnit.MILLISECONDS);
                    transactionTemplate.executeWithoutResult(_ -> repository.markAsPublished(List.of(event.id()), INSTANCE_ID));
                } catch (Exception exception) {
                    transactionTemplate.executeWithoutResult(_ -> repository.updateError(event.id(), INSTANCE_ID, MAX_RETRIES, Instant.now(clock).plusSeconds(60), errorMessage(exception)));
                    log.error("Failed to publish outbox message {}", event.id(), exception);
                    break;
                }
            }
        }
    }

    private String errorMessage(Exception exception) {
        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
        String message = cause.getMessage() == null ? cause.getClass().getName() : cause.getMessage();
        return message.substring(0, Math.min(message.length(), 2000));
    }
}
