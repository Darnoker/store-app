package com.github.darnoker.orderservice.outbox;

import com.github.darnoker.common.async.AsyncMessagePublisher;
import com.github.darnoker.common.async.OutboundMessage;
import com.github.darnoker.orderservice.outbox.lease.OutboxLeaseGuard;
import com.github.darnoker.orderservice.outbox.lease.OutboxLeaseManager;
import com.github.darnoker.orderservice.outbox.persistence.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private final AsyncMessagePublisher publisher;

    private final OutboxRepository repository;

    private final Clock clock;

    private final TransactionTemplate transactionTemplate;

    private final OutboxRelayProperties properties;

    private final OutboxLeaseManager leaseManager;

    private static final UUID INSTANCE_ID = UUID.randomUUID();

    private static final int MAX_RETRIES = 10;

    @Scheduled(fixedDelayString = "${outbox.relay.polling-interval-ms:10000}")
    public void relay() {
        final List<OutboundMessage> claimedMessages = transactionTemplate.execute((_) ->
                repository.claimBatch(properties.batchSize(), INSTANCE_ID, Instant.now(clock).plus(properties.leaseDuration())).stream()
                        .map(OutboxMessageMapper::mapToOutboundMessage)
                        .toList());

        if (claimedMessages.isEmpty()) {
            return;
        }

        final Set<UUID> publishedIds = new HashSet<>();
        final Set<UUID> claimedIds = claimedMessages.stream()
                .map(OutboundMessage::id)
                .collect(Collectors.toSet());

        try (OutboxLeaseGuard leaseGuard = leaseManager.startClaimedBatch(claimedIds, INSTANCE_ID)) {
            for (OutboundMessage message : claimedMessages) {
                if (!leaseGuard.isHeld()) {
                    log.warn("Stopping outbox relay because the lease for its current batch was lost");
                    break;
                }

                if (publish(message)) {
                    publishedIds.add(message.id());
                } else {
                    break;
                }
            }
        }

        if (!publishedIds.isEmpty()) {
            Integer markedAsPublished = transactionTemplate.execute(_ -> repository.markAsPublished(publishedIds, INSTANCE_ID));
            if (markedAsPublished == null || markedAsPublished != publishedIds.size()) {
                log.warn("Marked {} of {} outbox messages as published for instance {}",
                        markedAsPublished, publishedIds.size(), INSTANCE_ID);
            }
        }
    }

    private boolean publish(OutboundMessage message) {
        CompletableFuture<Void> publication = null;
        try {
            publication = publisher.publish(message).toCompletableFuture();
            publication.get(properties.publishTimeout().toMillis(), TimeUnit.MILLISECONDS);

            return true;
        } catch (TimeoutException exception) {
            publication.cancel(true);
            log.error("Timed out publishing outbox message {} after {}", message.id(), properties.publishTimeout(), exception);
            updateError(message, exception);
        } catch (InterruptedException exception) {
            updateError(message, exception);
            Thread.currentThread().interrupt();
        } catch (ExecutionException | RuntimeException exception) {
            log.error("Failed to publish outbox message {}", message.id(), exception);
            updateError(message, exception);
        }
        return false;
    }

    private void updateError(OutboundMessage message, Exception exception) {
        transactionTemplate.executeWithoutResult(_ -> repository.updateError(
                message.id(),
                INSTANCE_ID,
                MAX_RETRIES,
                Instant.now(clock).plusSeconds(60),
                errorMessage(exception)
        ));
    }

    private String errorMessage(Exception exception) {
        Throwable cause = exception instanceof ExecutionException && exception.getCause() != null
                ? exception.getCause()
                : exception;
        String message = cause.getMessage() == null ? cause.getClass().getName() : cause.getMessage();
        return message.substring(0, Math.min(message.length(), 2000));
    }
}
