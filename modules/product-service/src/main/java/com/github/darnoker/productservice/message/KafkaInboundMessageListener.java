package com.github.darnoker.productservice.message;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class KafkaInboundMessageListener {

    private static final String ORDER_TOPIC = "order-topic";

    private final MessageMapper messageMapper;

    private final InboundMessageDispatcher inboundMessageDispatcher;

    @KafkaListener(
            topics = ORDER_TOPIC,
            groupId = "product-service.inventory"
    )
    void onMessage(ConsumerRecord<String, String> record) {
        inboundMessageDispatcher.dispatch(messageMapper.fromKafka(record.value()));
    }
}
