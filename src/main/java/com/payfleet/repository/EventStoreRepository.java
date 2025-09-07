package com.payfleet.repository;

import com.payfleet.model.EventStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event Store Repository - Database operations for event sourcing
 */
@Repository
public interface EventStoreRepository extends JpaRepository<EventStore, Long> {

    /**
     * Find all events for a specific aggregate ordered by version
     */
    List<EventStore> findByAggregateIdOrderByVersionAsc(String aggregateId);

    /**
     * Find events for aggregate from specific version
     */
    List<EventStore> findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(
            String aggregateId, Long version);

    /**
     * Find events by aggregate type
     */
    List<EventStore> findByAggregateTypeOrderByCreatedAtAsc(String aggregateType);

    /**
     * Find events by event type
     */
    List<EventStore> findByEventTypeOrderByCreatedAtAsc(String eventType);

    /**
     * Find events by user
     */
    List<EventStore> findByUserIdOrderByCreatedAtAsc(String userId);

    /**
     * Find events by correlation ID
     */
    List<EventStore> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);

    /**
     * Find events within date range
     */
    List<EventStore> findByCreatedAtBetweenOrderByCreatedAtAsc(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get next version number for aggregate
     */
    @Query("SELECT COALESCE(MAX(e.version), 0) + 1 FROM EventStore e WHERE e.aggregateId = :aggregateId")
    Long getNextVersionForAggregate(@Param("aggregateId") String aggregateId);

    /**
     * Count events for aggregate
     */
    long countByAggregateId(String aggregateId);

    /**
     * Find events for audit trail by multiple criteria
     */
    @Query("SELECT e FROM EventStore e WHERE " +
            "(:aggregateType IS NULL OR e.aggregateType = :aggregateType) AND " +
            "(:eventType IS NULL OR e.eventType = :eventType) AND " +
            "(:userId IS NULL OR e.userId = :userId) AND " +
            "(:startDate IS NULL OR e.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR e.createdAt <= :endDate) " +
            "ORDER BY e.createdAt ASC")
    List<EventStore> findEventsForAuditTrail(
            @Param("aggregateType") String aggregateType,
            @Param("eventType") String eventType,
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
