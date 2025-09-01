package com.payfleet.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Payment Initiation Request DTO
 * <p>
 * Captures information needed to initiate a new payment
 */
public class PaymentInitiationRequest {

    @NotBlank(message = "Source account number is required")
    @Pattern(regexp = "^ACC-\\d{8}-\\d{6}$",
            message = "Invalid account number format. Expected: ACC-YYYYMMDD-XXXXXX")
    private String fromAccountNumber;

    @NotBlank(message = "Destination account number is required")
    @Pattern(regexp = "^ACC-\\d{8}-\\d{6}$",
            message = "Invalid account number format. Expected: ACC-YYYYMMDD-XXXXXX")
    private String toAccountNumber;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be at least 0.01")
    @DecimalMax(value = "100000.00", message = "Payment amount cannot exceed 100,000.00")
    @Digits(integer = 8, fraction = 2, message = "Payment amount must have at most 8 digits and 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency = "USD";

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Pattern(regexp = "^[\\w\\s.,!?-]*$",
            message = "Description contains invalid characters")
    private String description;

    @Size(max = 100, message = "Idempotency key must not exceed 100 characters")
    private String idempotencyKey;

    // Constructors
    public PaymentInitiationRequest() {
    }

    public PaymentInitiationRequest(String fromAccountNumber, String toAccountNumber,
                                    BigDecimal amount, String currency, String description) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "PaymentInitiationRequest{" +
                "fromAccountNumber='" + fromAccountNumber + '\'' +
                ", toAccountNumber='" + toAccountNumber + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
