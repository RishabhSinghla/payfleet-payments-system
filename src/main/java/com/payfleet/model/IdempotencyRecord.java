package com.payfleet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Idempotency Record Entity - Tracks processed requests to prevent duplicates
 * <p>
 * Enterprise Pattern: Idempotency Key Management
 * Banking Context: Prevents duplicate payment processing and charges
 */
@Entity
@Table(name = "idempotency_records",
        indexes = {
                @Index(name = "idx_idempotency_key", columnList = "idempotency_key"),
                @Index(name = "idx_resource_type", columnList = "resource_type"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_status", columnList = "status")
        })
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Idempotency Key - Unique identifier for request deduplication
     */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    /**
     * Resource Type - Type of resource being created/modified
     */
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    /**
     * Resource ID - ID of the created/modified resource
     */
    @Column(name = "resource_id", length = 255)
    private String resourceId;

    /**
     * User ID - Who made the request
     */
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    /**
     * Request Hash - Hash of the request payload for validation
     */
    @Column(name = "request_hash", length = 64)
    private String requestHash;

    /**
     * Response Body - Cached response for duplicate requests
     */
    @Lob
    @Column(name = "response_body")
    private String responseBody;

    /**
     * HTTP Status Code - Status code of the original response
     */
    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    /**
     * Processing Status - Current status of the request
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IdempotencyStatus status;

    /**
     * Expiry Time - When this record should be cleaned up
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Processing Started At - When processing began (for timeout detection)
     */
    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    /**
     * Audit timestamps
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default Constructor
    public IdempotencyRecord() {
    }

    // Constructor for new idempotency records
    public IdempotencyRecord(String idempotencyKey, String resourceType, String userId,
                             String requestHash, LocalDateTime expiresAt) {
        this.idempotencyKey = idempotencyKey;
        this.resourceType = resourceType;
        this.userId = userId;
        this.requestHash = requestHash;
        this.status = IdempotencyStatus.PROCESSING;
        this.expiresAt = expiresAt;
        this.processingStartedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public void setStatus(IdempotencyStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getProcessingStartedAt() {
        return processingStartedAt;
    }

    public void setProcessingStartedAt(LocalDateTime processingStartedAt) {
        this.processingStartedAt = processingStartedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Business Methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isProcessing() {
        return status == IdempotencyStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return status == IdempotencyStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == IdempotencyStatus.FAILED;
    }

    public boolean isProcessingTimedOut(long timeoutMinutes) {
        if (processingStartedAt == null) return false;
        return LocalDateTime.now().isAfter(processingStartedAt.plusMinutes(timeoutMinutes));
    }
}
