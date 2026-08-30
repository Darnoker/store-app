package com.github.darnoker.productservice.inventory;

import com.github.darnoker.common.async.InboundMessage;
import com.github.darnoker.common.async.InboundMessageHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedMessageHandler implements InboundMessageHandler {


    @Override
    public String eventType() {
        return OrderEventType.ORDER_CREATED.name();
    }

    @Override
    public void consume(InboundMessage message) {

    }
}
