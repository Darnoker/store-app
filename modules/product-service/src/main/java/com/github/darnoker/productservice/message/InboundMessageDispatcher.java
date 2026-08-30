package com.github.darnoker.productservice.message;

import com.github.darnoker.common.async.InboundMessage;
import com.github.darnoker.common.async.InboundMessageHandler;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class InboundMessageDispatcher {

    private final Map<String, InboundMessageHandler> inboundMessageHandlers;

    public InboundMessageDispatcher(Collection<InboundMessageHandler> inboundMessageHandlers) {
        this.inboundMessageHandlers = inboundMessageHandlers.stream()
                .collect(Collectors.toMap(InboundMessageHandler::eventType, Function.identity()));
    }

    public void dispatch(InboundMessage message) {
        InboundMessageHandler handler = inboundMessageHandlers.get(message.eventType());
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported inbound event type: " + message.eventType());
        }
        handler.consume(message);
    }
}
