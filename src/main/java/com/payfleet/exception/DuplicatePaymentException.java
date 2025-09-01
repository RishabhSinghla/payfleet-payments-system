package com.payfleet.exception;

/**
 * Exception thrown when a duplicate payment is attempted
 */
public class DuplicatePaymentException extends PayFleetException {

    private final String idempotencyKey;
    private final String existingPaymentReference;

    public DuplicatePaymentException(String idempotencyKey, String existingPaymentReference) {
        super("Duplicate payment detected for idempotency key: " + idempotencyKey,
                "DUPLICATE_PAYMENT");
        this.idempotencyKey = idempotencyKey;
        this.existingPaymentReference = existingPaymentReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getExistingPaymentReference() {
        return existingPaymentReference;
    }
}
