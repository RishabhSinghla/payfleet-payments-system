package com.payfleet.repository;

import com.payfleet.model.IdempotencyRecord;
import com.payfleet.model.IdempotencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Idempotency Repository - Database operations for idempotency records
 */
@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {

    /**
     * Find idempotency record by key
     */
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find records by status
     */
    List<IdempotencyRecord> findByStatus(IdempotencyStatus status);

    /**
     * Count records by status
     */
    long countByStatus(IdempotencyStatus status);

    /**
     * Find records by user
     */
    List<IdempotencyRecord> findByUserId(String userId);

    /**
     * Find records by resource type
     */
    List<IdempotencyRecord> findByResourceType(String resourceType);

    /**
     * Find expired records for cleanup
     */
    @Query("SELECT i FROM IdempotencyRecord i WHERE i.expiresAt < :now")
    List<IdempotencyRecord> findExpiredRecords(@Param("now") LocalDateTime now);

    /**
     * Find processing records that have timed out
     */
    @Query("SELECT i FROM IdempotencyRecord i WHERE i.status = 'PROCESSING' " +
            "AND i.processingStartedAt < :timeoutThreshold")
    List<IdempotencyRecord> findTimedOutProcessingRecords(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    /**
     * Find records created within date range
     */
    List<IdempotencyRecord> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Check if idempotency key exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);
}
