package com.github.darnoker.productservice.message.inbox.persistence;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InboxMessageId implements Serializable {

    private String consumerName;

    private UUID eventId;
}
