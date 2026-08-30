package com.github.darnoker.orderservice.async;

import com.github.darnoker.common.async.OutboundMessage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KafkaAsyncMessagePublisherTest {

    @Test
    void publishesVersionedEnvelopeWithJsonPayload() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        JsonMapper objectMapper = JsonMapper.builder().build();
        UUID messageId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-08-28T19:00:00Z");
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("order-topic", aggregateId.toString(), "sent-message");
        when(kafkaTemplate.send(eq("order-topic"), eq(aggregateId.toString()), anyString()))
                .thenReturn(CompletableFuture.completedFuture(new SendResult<>(producerRecord, null)));

        KafkaAsyncMessagePublisher publisher = new KafkaAsyncMessagePublisher(kafkaTemplate, objectMapper);
        publisher.publish(new OutboundMessage(
                messageId,
                "order-topic",
                aggregateId,
                "ORDER_CREATED",
                occurredAt,
                "{\"orderId\":\"" + aggregateId + "\"}"
        )).toCompletableFuture().join();

        org.mockito.ArgumentCaptor<String> payloadCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(kafkaTemplate).send(eq("order-topic"), eq(aggregateId.toString()), payloadCaptor.capture());
        JsonNode sentEnvelope = objectMapper.readTree(payloadCaptor.getValue());

        assertEquals(messageId.toString(), sentEnvelope.get("id").asString());
        assertEquals(aggregateId.toString(), sentEnvelope.get("aggregateId").asString());
        assertEquals("ORDER_CREATED", sentEnvelope.get("eventType").asString());
        assertEquals(occurredAt.toString(), sentEnvelope.get("occurredAt").asString());
        assertEquals(1, sentEnvelope.get("schemaVersion").asInt());
        assertTrue(sentEnvelope.get("payload").isObject());
        assertEquals(aggregateId.toString(), sentEnvelope.get("payload").get("orderId").asString());
    }
}
