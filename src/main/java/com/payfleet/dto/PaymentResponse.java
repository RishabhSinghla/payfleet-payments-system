package com.payfleet.dto;

import com.payfleet.model.Payment;
import com.payfleet.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Response DTO
 * <p>
 * Returns payment information without sensitive internal details
 */
public class PaymentResponse {

    private Long id;
    private String paymentReference;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String description;
    private String initiatedByUsername;
    private String processingDetails;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public PaymentResponse() {
    }

    // Constructor from Payment entity
    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.paymentReference = payment.getPaymentReference();
        this.fromAccountNumber = payment.getFromAccount().getAccountNumber();
        this.toAccountNumber = payment.getToAccount().getAccountNumber();
        this.amount = payment.getAmount();
        this.currency = payment.getCurrency();
        this.status = payment.getStatus();
        this.description = payment.getDescription();
        this.initiatedByUsername = payment.getInitiatedBy().getUsername();
        this.processingDetails = payment.getProcessingDetails();
        this.failureReason = payment.getFailureReason();
        this.createdAt = payment.getCreatedAt();
        this.processedAt = payment.getProcessedAt();
        this.updatedAt = payment.getUpdatedAt();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
