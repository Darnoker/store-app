package com.github.darnoker.orderservice.outbox.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface JpaOutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity> findAllByPublished(boolean published);

    @Modifying
    @Query(nativeQuery = true,
            value = """
                    UPDATE outbox_events
                    SET published = true
                    WHERE id in :ids
                    """
    )
    int markAsPublished(@Param("id") Collection<UUID> ids);
}
