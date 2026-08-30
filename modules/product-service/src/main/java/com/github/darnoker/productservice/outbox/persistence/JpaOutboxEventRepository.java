package com.github.darnoker.productservice.outbox.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface JpaOutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE outbox_events SET status = 'PUBLISHED', locked_by = NULL, locked_until = NULL, last_error = NULL WHERE id IN (:ids) AND status = 'PROCESSING' AND locked_by = :instanceId")
    int markAsPublished(@Param("ids") Collection<UUID> ids, @Param("instanceId") UUID instanceId);

    @Query(nativeQuery = true, value = """
            WITH events_to_claim AS (
                SELECT id FROM outbox_events
                WHERE (status = 'PENDING' AND (next_attempt_at IS NULL OR next_attempt_at <= NOW()))
                   OR (status = 'PROCESSING' AND locked_until <= NOW())
                ORDER BY created_at, id LIMIT :batchSize FOR UPDATE SKIP LOCKED
            )
            UPDATE outbox_events SET status = 'PROCESSING', locked_by = :instanceId, locked_until = :leaseUntil
            WHERE id IN (SELECT id FROM events_to_claim) RETURNING *
            """)
    List<OutboxEventEntity> claimBatch(@Param("batchSize") int batchSize, @Param("instanceId") UUID instanceId, @Param("leaseUntil") Instant leaseUntil);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE outbox_events SET locked_until = :leaseUntil WHERE id IN (:ids) AND status = 'PROCESSING' AND locked_by = :instanceId AND locked_until > NOW()")
    int renewLease(@Param("ids") Collection<UUID> ids, @Param("instanceId") UUID instanceId, @Param("leaseUntil") Instant leaseUntil);

    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE outbox_events SET retry_count = retry_count + 1,
                status = CASE WHEN retry_count + 1 >= :maxRetries THEN 'FAILED' ELSE 'PENDING' END,
                next_attempt_at = CASE WHEN retry_count + 1 >= :maxRetries THEN NULL ELSE :nextAttemptAt END,
                last_error = :error, locked_by = NULL, locked_until = NULL
            WHERE id = :id AND status = 'PROCESSING' AND locked_by = :instanceId
            """)
    void updateError(@Param("id") UUID id, @Param("instanceId") UUID instanceId, @Param("maxRetries") int maxRetries, @Param("nextAttemptAt") Instant nextAttemptAt, @Param("error") String error);
}
