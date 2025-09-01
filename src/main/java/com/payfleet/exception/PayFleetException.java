package com.payfleet.exception;

/**
 * Base Exception for PayFleet Business Logic Errors
 * <p>
 * Enterprise Pattern: Custom Exception Hierarchy
 * Provides a common base for all business-specific exceptions
 */
public abstract class PayFleetException extends RuntimeException {

    private final String errorCode;

    public PayFleetException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PayFleetException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
