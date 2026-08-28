package com.github.darnoker.orderservice.async;

import com.github.darnoker.common.async.AsyncMessagePublisher;
import com.github.darnoker.common.async.OutboundMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Component
@Slf4j
@RequiredArgsConstructor
class KafkaAsyncMessagePublisher implements AsyncMessagePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public CompletionStage<Void> publish(OutboundMessage message) {
        KafkaEnvelope envelope = new KafkaEnvelope(message.id(), message.aggregateId(), message.eventType(), message.occuredAt(), message.payload());
        return kafkaTemplate.send(message.destination(), message.aggregateId().toString(), objectMapper.writeValueAsString(envelope))
                .thenApply(result -> {
                    log.info("Message {} of event type {} sent to topic {} ", message.id(), message.eventType(), result.getProducerRecord().topic());
                    return null;
                });
    }

    private record KafkaEnvelope(UUID id, UUID aggregateId, String eventType, Instant occurredAt, String payload) {
    }
}
