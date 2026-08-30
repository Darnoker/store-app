package com.github.darnoker.productservice.message.inbox.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inbox_messages")
@IdClass(InboxMessageId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
class InboxMessageEntity {

    @Id
    @Column(name = "consumer_name", nullable = false)
    private String consumerName;

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
}
