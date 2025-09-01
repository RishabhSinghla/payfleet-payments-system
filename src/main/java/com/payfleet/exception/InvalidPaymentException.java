package com.payfleet.exception;

/**
 * Exception thrown for invalid payment requests
 */
public class InvalidPaymentException extends PayFleetException {

    public InvalidPaymentException(String message) {
        super(message, "INVALID_PAYMENT");
    }

    public InvalidPaymentException(String message, Throwable cause) {
        super(message, "INVALID_PAYMENT", cause);
    }
}
