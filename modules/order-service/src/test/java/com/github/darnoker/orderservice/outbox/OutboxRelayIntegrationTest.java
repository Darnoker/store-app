package com.github.darnoker.orderservice.outbox;

import com.github.darnoker.orderservice.order.CurrencyCode;
import com.github.darnoker.orderservice.order.OrderTopics;
import com.github.darnoker.orderservice.order.event.OrderCreated;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles({"test", "outbox-relay-test"})
class OutboxRelayIntegrationTest {

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxRelay outboxRelay;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @BeforeEach
    void prepareOutbox() {
        jdbcTemplate.update("DELETE FROM outbox_events");
    }

    @Test
    void persistsPublishesAndMarksAnOutboxEventAsPublished() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        outboxService.save(
                orderId,
                OrderTopics.ORDER_TOPIC,
                EventType.ORDER_CREATED,
                new OrderCreated(orderId, customerId, CurrencyCode.PLN, Instant.now(), List.of())
        );
        UUID eventId = jdbcTemplate.queryForObject(
                "SELECT id FROM outbox_events WHERE aggregate_id = ?", UUID.class, orderId);

        try (KafkaConsumer<String, String> consumer = kafkaConsumer()) {
            consumer.subscribe(List.of(OrderTopics.ORDER_TOPIC));
            consumer.poll(Duration.ofMillis(100));
            outboxRelay.relay();

            assertTrue(receivesEvent(consumer, eventId, orderId));
        }
        assertEquals("PUBLISHED", jdbcTemplate.queryForObject(
                "SELECT status FROM outbox_events WHERE id = ?", String.class, eventId));
        assertNull(jdbcTemplate.queryForObject(
                "SELECT locked_by FROM outbox_events WHERE id = ?", UUID.class, eventId));
    }

    private KafkaConsumer<String, String> kafkaConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "outbox-relay-integration-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new KafkaConsumer<>(properties, new StringDeserializer(), new StringDeserializer());
    }

    private boolean receivesEvent(KafkaConsumer<String, String> consumer, UUID eventId, UUID orderId) {
        Instant deadline = Instant.now().plusSeconds(10);
        while (Instant.now().isBefore(deadline)) {
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(250))) {
                if (record.key().equals(orderId.toString()) && record.value().contains(eventId.toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}
