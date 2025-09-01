package com.payfleet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity - Represents payment transactions in the system
 * <p>
 * Enterprise Pattern: Core Financial Transaction Model
 * - Tracks money movement between accounts
 * - Maintains complete audit trail
 * - Supports idempotency for reliable processing
 * - Links to source and destination accounts
 * <p>
 * Banking Context: Foundation of all financial transactions
 */
@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payment_reference", columnList = "payment_reference"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_from_account", columnList = "from_account_id"),
                @Index(name = "idx_to_account", columnList = "to_account_id")
        })
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Payment Reference - Unique identifier for external tracking
     * Format: PAY-YYYYMMDD-XXXXXX (e.g., PAY-20250831-789123)
     */
    @Column(name = "payment_reference", nullable = false, unique = true, length = 25)
    @NotBlank(message = "Payment reference is required")
    @Size(max = 25, message = "Payment reference must not exceed 25 characters")
    private String paymentReference;

    /**
     * Source Account - Account money is debited from
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    @NotNull(message = "Source account is required")
    private Account fromAccount;

    /**
     * Destination Account - Account money is credited to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    @NotNull(message = "Destination account is required")
    private Account toAccount;

    /**
     * Payment Amount - Amount to be transferred
     * Using BigDecimal for exact decimal arithmetic
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.01", message = "Payment amount must be at least 0.01")
    private BigDecimal amount;

    /**
     * Currency Code - ISO currency code for the transaction
     */
    @Column(name = "currency", nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;

    /**
     * Payment Status - Current state of the payment
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Payment Description - Purpose or description of the payment
     */
    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Idempotency Key - Prevents duplicate processing
     */
    @Column(name = "idempotency_key", length = 100)
    @Size(max = 100, message = "Idempotency key must not exceed 100 characters")
    private String idempotencyKey;

    /**
     * Initiated By - User who initiated the payment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by_user_id", nullable = false)
    @NotNull(message = "Initiating user is required")
    private User initiatedBy;

    /**
     * Processing Details - Information about payment processing
     */
    @Column(name = "processing_details", length = 1000)
    private String processingDetails;

    /**
     * Failure Reason - Reason for payment failure (if applicable)
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * Processed At - Timestamp when payment was completed/failed
     */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Audit Fields - Track record creation and modification
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default Constructor
    public Payment() {
    }

    // Constructor for creating new payments
    public Payment(String paymentReference, Account fromAccount, Account toAccount,
                   BigDecimal amount, String currency, String description, User initiatedBy) {
        this.paymentReference = paymentReference;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.initiatedBy = initiatedBy;
        this.status = PaymentStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Account getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Account fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public User getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(User initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public String getProcessingDetails() {
        return processingDetails;
    }

    public void setProcessingDetails(String processingDetails) {
        this.processingDetails = processingDetails;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Business Logic Methods

    /**
     * Mark payment as completed
     */
    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Mark payment as failed with reason
     */
    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Check if payment is in pending status
     */
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    /**
     * Check if payment is completed
     */
    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    /**
     * Check if payment has failed
     */
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }
}
