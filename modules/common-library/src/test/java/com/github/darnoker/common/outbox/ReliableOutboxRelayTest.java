package com.github.darnoker.common.outbox;

import com.github.darnoker.common.async.OutboundMessage;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReliableOutboxRelayTest {
    private static final Instant NOW = Instant.parse("2026-08-30T12:00:00Z");

    @Test
    void marksAnAcknowledgedMessageAsPublished() {
        OutboundMessage message = message();
        RecordingRepository repository = new RecordingRepository(List.of(message));
        ReliableOutboxRelay relay = relay(repository, outboundMessage -> CompletableFuture.completedFuture(null));

        relay.relay();

        assertEquals(List.of(message.id()), repository.publishedIds);
        assertTrue(repository.errors.isEmpty());
    }

    @Test
    void leavesFailedMessagePendingForARetry() {
        OutboundMessage message = message();
        RecordingRepository repository = new RecordingRepository(List.of(message));
        ReliableOutboxRelay relay = relay(repository,
                outboundMessage -> CompletableFuture.failedFuture(new IllegalStateException("Kafka unavailable")));

        relay.relay();

        assertTrue(repository.publishedIds.isEmpty());
        assertEquals(1, repository.errors.size());
        ErrorUpdate error = repository.errors.getFirst();
        assertEquals(message.id(), error.id());
        assertEquals(NOW.plusSeconds(60), error.nextAttemptAt());
        assertTrue(error.message().contains("Kafka unavailable"));
    }

    private ReliableOutboxRelay relay(RecordingRepository repository,
                                      com.github.darnoker.common.async.AsyncMessagePublisher publisher) {
        TransactionRunner transactionRunner = new TransactionRunner() {
            @Override
            public <T> T execute(java.util.function.Supplier<T> action) {
                return action.get();
            }

            @Override
            public void executeWithoutResult(Runnable action) {
                action.run();
            }
        };
        return new ReliableOutboxRelay(
                publisher,
                repository,
                Clock.fixed(NOW, ZoneOffset.UTC),
                transactionRunner,
                new OutboxRelaySettings(10, Duration.ofSeconds(1), Duration.ofSeconds(10), Duration.ofSeconds(2), 3),
                (task, interval) -> () -> { }
        );
    }

    private OutboundMessage message() {
        return new OutboundMessage(UUID.randomUUID(), "order-topic", UUID.randomUUID(), "ORDER_CREATED", NOW, "{}");
    }

    private static final class RecordingRepository implements OutboxRelayRepository {
        private final List<OutboundMessage> messages;
        private final List<UUID> publishedIds = new ArrayList<>();
        private final List<ErrorUpdate> errors = new ArrayList<>();

        private RecordingRepository(List<OutboundMessage> messages) {
            this.messages = messages;
        }

        @Override
        public List<OutboundMessage> claimBatch(int batchSize, UUID instanceId, Instant leaseUntil) {
            return messages;
        }

        @Override
        public int markAsPublished(Collection<UUID> ids, UUID instanceId) {
            publishedIds.addAll(ids);
            return ids.size();
        }

        @Override
        public int renewLease(Collection<UUID> ids, UUID instanceId, Instant leaseUntil) {
            return ids.size();
        }

        @Override
        public void updateError(UUID id, UUID instanceId, int maxRetries, Instant nextAttemptAt, String error) {
            errors.add(new ErrorUpdate(id, nextAttemptAt, error));
        }
    }

    private record ErrorUpdate(UUID id, Instant nextAttemptAt, String message) {
    }
}
