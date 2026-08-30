package com.github.darnoker.productservice.message.inbox.persistence;

import com.github.darnoker.productservice.message.inbox.model.InboxMessage;

public interface InboxRepository {

    /**
     * Records a message when it has not been processed by this consumer before.
     *
     * @return {@code true} for a new message, {@code false} for a duplicate
     */
    boolean recordIfAbsent(InboxMessage message);
}
