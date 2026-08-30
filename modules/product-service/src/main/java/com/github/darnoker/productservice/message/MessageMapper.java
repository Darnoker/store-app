package com.github.darnoker.productservice.message;

import com.github.darnoker.common.async.InboundMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
final class MessageMapper {
    private final ObjectMapper objectMapper;

    InboundMessage fromKafka(String serializedEnvelope) {
        JsonNode envelope = objectMapper.readTree(serializedEnvelope);
        return new InboundMessage(
                UUID.fromString(requiredText(envelope, "id")),
                UUID.fromString(requiredText(envelope, "aggregateId")),
                requiredText(envelope, "eventType"),
                Instant.parse(requiredText(envelope, "occurredAt")),
                envelope.required("schemaVersion").asInt(),
                envelope.required("payload").toString()
        );
    }

    private String requiredText(JsonNode envelope, String fieldName) {
        JsonNode field = envelope.required(fieldName);
        if (!field.isString() || field.asString().isBlank()) {
            throw new IllegalArgumentException("Envelope field '" + fieldName + "' must be a non-blank string");
        }
        return field.asString();
    }
}
