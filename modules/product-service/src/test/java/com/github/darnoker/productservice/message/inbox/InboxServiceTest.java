package com.github.darnoker.productservice.message.inbox;

import com.github.darnoker.productservice.message.inbox.model.InboxMessage;
import com.github.darnoker.productservice.message.inbox.persistence.InboxRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InboxServiceTest {

    @Test
    void recordsMessageWithTheProvidedConsumerAndTheClockTime() {
        InboxRepository repository = mock(InboxRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-30T12:00:00Z"), ZoneOffset.UTC);
        InboxService inboxService = new InboxService(repository, clock);
        UUID eventId = UUID.randomUUID();
        when(repository.recordIfAbsent(org.mockito.ArgumentMatchers.any(InboxMessage.class))).thenReturn(true);

        assertTrue(inboxService.recordIfAbsent("product-service.inventory", eventId));

        ArgumentCaptor<InboxMessage> messageCaptor = ArgumentCaptor.forClass(InboxMessage.class);
        verify(repository).recordIfAbsent(messageCaptor.capture());
        assertEquals("product-service.inventory", messageCaptor.getValue().consumerName());
        assertEquals(eventId, messageCaptor.getValue().eventId());
        assertEquals(Instant.now(clock), messageCaptor.getValue().receivedAt());
    }
}
