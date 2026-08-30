package com.github.darnoker.common.async;

public interface InboundMessageHandler {
    String eventType();
    void consume(InboundMessage message);
}
