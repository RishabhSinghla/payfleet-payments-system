package com.payfleet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Event Store Entity - Persistent storage for all domain events
 * <p>
 * Enterprise Pattern: Event Sourcing with Persistent Storage
 * Banking Context: Immutable audit trail for regulatory compliance
 */
@Entity
@Table(name = "event_store",
        indexes = {
                @Index(name = "idx_aggregate_id", columnList = "aggregate_id"),
                @Index(name = "idx_event_type", columnList = "event_type"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_version", columnList = "aggregate_id, version")
        })
public class EventStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Aggregate ID - Links events to specific domain entities
     */
    @Column(name = "aggregate_id", nullable = false, length = 50)
    private String aggregateId;

    /**
     * Aggregate Type - Type of domain entity (Payment, Account, etc.)
     */
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    /**
     * Event Type - Specific type of event
     */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /**
     * Event Data - JSON payload containing event details
     */
    @Lob
    @Column(name = "event_data", nullable = false)
    private String eventData;

    /**
     * Version - Sequence number for event ordering
     */
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * User Context - Who initiated the event
     */
    @Column(name = "user_id", length = 50)
    private String userId;

    /**
     * Correlation ID - Links related events across services
     */
    @Column(name = "correlation_id", length = 50)
    private String correlationId;

    /**
     * Metadata - Additional context information
     */
    @Lob
    @Column(name = "metadata")
    private String metadata;

    /**
     * Created timestamp - When event was stored (immutable)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default Constructor
    public EventStore() {
    }

    // Constructor for event creation
    public EventStore(String aggregateId, String aggregateType, String eventType,
                      String eventData, Long version, String userId, String correlationId) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.eventData = eventData;
        this.version = version;
        this.userId = userId;
        this.correlationId = correlationId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
