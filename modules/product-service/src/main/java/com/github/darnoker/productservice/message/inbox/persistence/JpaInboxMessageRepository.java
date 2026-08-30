package com.github.darnoker.productservice.message.inbox.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

interface JpaInboxMessageRepository extends JpaRepository<InboxMessageEntity, InboxMessageId> {

    @Modifying
    @Query(value = """
            INSERT INTO inbox_messages (consumer_name, event_id, received_at)
            VALUES (:consumerName, :eventId, :receivedAt)
            ON CONFLICT (consumer_name, event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("consumerName") String consumerName,
            @Param("eventId") UUID eventId,
            @Param("receivedAt") Instant receivedAt
    );
}
