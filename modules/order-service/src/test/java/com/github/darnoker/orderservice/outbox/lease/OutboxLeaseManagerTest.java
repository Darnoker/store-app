package com.github.darnoker.orderservice.outbox.lease;

import com.github.darnoker.orderservice.outbox.OutboxRelayProperties;
import com.github.darnoker.orderservice.outbox.persistence.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxLeaseManagerTest {

    @Test
    void keepsGuardHeldWhenEveryMessageLeaseIsRenewed() {
        OutboxRepository repository = mock(OutboxRepository.class);
        TaskScheduler scheduler = mock(TaskScheduler.class);
        TransactionTemplate transactionTemplate = transactionTemplateThatExecutesCallbacks();
        ScheduledFuture<?> heartbeat = mock(ScheduledFuture.class);
        ArgumentCaptor<Runnable> heartbeatCaptor = ArgumentCaptor.forClass(Runnable.class);
        Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID());
        UUID instanceId = UUID.randomUUID();
        doReturn(heartbeat).when(scheduler)
                .scheduleAtFixedRate(heartbeatCaptor.capture(), eq(Duration.ofSeconds(10)));
        when(repository.renewLease(eq(ids), eq(instanceId), any())).thenReturn(ids.size());

        OutboxLeaseManager manager = manager(repository, scheduler, transactionTemplate);
        OutboxLeaseGuard guard = manager.startClaimedBatch(ids, instanceId);
        heartbeatCaptor.getValue().run();

        assertTrue(guard.isHeld());
        verify(repository).renewLease(eq(ids), eq(instanceId), eq(Instant.parse("2026-08-29T12:00:45Z")));
        guard.close();
        verify(heartbeat).cancel(false);
    }

    @Test
    void marksGuardAsLostWhenNotEveryMessageLeaseIsRenewed() {
        OutboxRepository repository = mock(OutboxRepository.class);
        TaskScheduler scheduler = mock(TaskScheduler.class);
        TransactionTemplate transactionTemplate = transactionTemplateThatExecutesCallbacks();
        ScheduledFuture<?> heartbeat = mock(ScheduledFuture.class);
        ArgumentCaptor<Runnable> heartbeatCaptor = ArgumentCaptor.forClass(Runnable.class);
        Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID());
        UUID instanceId = UUID.randomUUID();
        doReturn(heartbeat).when(scheduler)
                .scheduleAtFixedRate(heartbeatCaptor.capture(), eq(Duration.ofSeconds(10)));
        when(repository.renewLease(eq(ids), eq(instanceId), any())).thenReturn(1);

        OutboxLeaseGuard guard = manager(repository, scheduler, transactionTemplate).startClaimedBatch(ids, instanceId);
        heartbeatCaptor.getValue().run();

        assertFalse(guard.isHeld());
    }

    private OutboxLeaseManager manager(
            OutboxRepository repository,
            TaskScheduler scheduler,
            TransactionTemplate transactionTemplate
    ) {
        return new OutboxLeaseManager(
                repository,
                Clock.fixed(Instant.parse("2026-08-29T12:00:00Z"), ZoneOffset.UTC),
                new OutboxRelayProperties(100, Duration.ofSeconds(15), Duration.ofSeconds(45), Duration.ofSeconds(10)),
                transactionTemplate,
                scheduler
        );
    }

    @SuppressWarnings("unchecked")
    private TransactionTemplate transactionTemplateThatExecutesCallbacks() {
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        when(transactionTemplate.execute(any())).thenAnswer(invocation ->
                ((TransactionCallback<Object>) invocation.getArgument(0)).doInTransaction(null));
        return transactionTemplate;
    }
}
