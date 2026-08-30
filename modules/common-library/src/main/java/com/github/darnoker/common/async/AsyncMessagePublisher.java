package com.github.darnoker.common.async;

import java.util.concurrent.CompletionStage;

public interface AsyncMessagePublisher {
    CompletionStage<Void> publish(OutboundMessage message);
}
