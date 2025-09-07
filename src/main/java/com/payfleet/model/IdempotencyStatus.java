package com.payfleet.model;

/**
 * Idempotency Status - Represents the processing state of idempotent requests
 */
public enum IdempotencyStatus {
    /**
     * Request is currently being processed
     */
    PROCESSING,

    /**
     * Request has been successfully completed
     */
    COMPLETED,

    /**
     * Request processing failed
     */
    FAILED,

    /**
     * Request processing timed out
     */
    TIMEOUT
}
