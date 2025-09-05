package com.payfleet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.payfleet.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Event DTO - Represents payment events in the Kafka message system
 * <p>
 * Enterprise Pattern: Event-Driven Architecture
 * Banking Context: Payment events drive notifications, reporting, and audit
 */
public class PaymentEvent {

    private String eventId;
    private String eventType;
    private Long paymentId;
    private String paymentReference;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String description;
    private String initiatedByUsername;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String processingDetails;
    private String failureReason;

    // Default constructor
    public PaymentEvent() {
        this.timestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }

    // Constructor for payment events
    public PaymentEvent(String eventType, Long paymentId, String paymentReference,
                        String fromAccountNumber, String toAccountNumber,
                        BigDecimal amount, String currency, PaymentStatus status,
                        String description, String initiatedByUsername) {
        this();
        this.eventType = eventType;
        this.paymentId = paymentId;
        this.paymentReference = paymentReference;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.description = description;
        this.initiatedByUsername = initiatedByUsername;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
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

    public String getInitiatedByUsername() {
        return initiatedByUsername;
    }

    public void setInitiatedByUsername(String initiatedByUsername) {
        this.initiatedByUsername = initiatedByUsername;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "eventType='" + eventType + '\'' +
                ", paymentReference='" + paymentReference + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}
