package com.github.darnoker.orderservice.async;

import com.github.darnoker.common.async.AsyncMessagePublisher;
import com.github.darnoker.common.async.EventEnvelope;
import com.github.darnoker.common.async.OutboundMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletionStage;

@Component
@Slf4j
@RequiredArgsConstructor
class KafkaAsyncMessagePublisher implements AsyncMessagePublisher {

    private static final int EVENT_SCHEMA_VERSION = 1;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public CompletionStage<Void> publish(OutboundMessage message) {
        EventEnvelope<JsonNode> envelope = new EventEnvelope<>(
                message.id(),
                message.aggregateId(),
                message.eventType(),
                message.occurredAt(),
                EVENT_SCHEMA_VERSION,
                objectMapper.readTree(message.payload())
        );
        return kafkaTemplate.send(message.destination(), message.aggregateId().toString(), objectMapper.writeValueAsString(envelope))
                .thenApply(result -> {
                    log.info("Message {} of event type {} sent to topic {} ", message.id(), message.eventType(), result.getProducerRecord().topic());
                    return null;
                });
    }

}
