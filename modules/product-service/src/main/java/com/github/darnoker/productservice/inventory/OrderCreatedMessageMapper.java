package com.github.darnoker.productservice.inventory;

import com.github.darnoker.common.async.InboundMessage;
import com.github.darnoker.productservice.inventory.model.ReserveStockCommand;
import com.github.darnoker.productservice.inventory.model.ReservedItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class OrderCreatedMessageMapper {

    private static final int SUPPORTED_SCHEMA_VERSION = 1;

    private final ObjectMapper objectMapper;

    ReserveStockCommand toReserveStockCommand(InboundMessage message) {
        if (!OrderEventType.ORDER_CREATED.name().equals(message.eventType())) {
            throw new IllegalArgumentException("Expected ORDER_CREATED event but received " + message.eventType());
        }
        if (message.schemaVersion() != SUPPORTED_SCHEMA_VERSION) {
            throw new IllegalArgumentException("Unsupported ORDER_CREATED schema version: " + message.schemaVersion());
        }

        JsonNode payload = objectMapper.readTree(message.payload());
        UUID orderId = UUID.fromString(requiredText(payload, "orderId"));
        if (!message.aggregateId().equals(orderId)) {
            throw new IllegalArgumentException("ORDER_CREATED aggregate ID must match its order ID");
        }

        JsonNode items = payload.required("items");
        if (!items.isArray() || items.isEmpty()) {
            throw new IllegalArgumentException("ORDER_CREATED items must be a non-empty array");
        }

        List<ReservedItem> reservedItems = new ArrayList<>();
        for (JsonNode item : items) {
            UUID productId = UUID.fromString(requiredText(item, "id"));
            reservedItems.add(new ReservedItem(productId, positiveWholeQuantity(item.required("quantity"))));
        }

        return new ReserveStockCommand(orderId, message.id(), reservedItems);
    }

    private String requiredText(JsonNode node, String fieldName) {
        JsonNode field = node.required(fieldName);
        if (!field.isTextual() || field.asString().isBlank()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' must be a non-blank string");
        }
        return field.asString();
    }

    private int positiveWholeQuantity(JsonNode quantity) {
        if (!quantity.isNumber()) {
            throw new IllegalArgumentException("Order item quantity must be a number");
        }

        try {
            int value = new BigDecimal(quantity.asString()).intValueExact();
            if (value <= 0) {
                throw new IllegalArgumentException("Order item quantity must be positive");
            }
            return value;
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Order item quantity must be a whole number", exception);
        }
    }
}
