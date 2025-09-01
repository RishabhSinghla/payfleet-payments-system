package com.payfleet.model;

/**
 * Payment Status - Represents the lifecycle of a payment transaction
 * <p>
 * Banking Context: Payments go through various states during processing
 */
public enum PaymentStatus {
    /**
     * Payment has been initiated but not yet processed
     */
    PENDING,

    /**
     * Payment is currently being processed
     */
    PROCESSING,

    /**
     * Payment has been successfully completed
     */
    COMPLETED,

    /**
     * Payment has failed due to various reasons
     */
    FAILED,

    /**
     * Payment has been cancelled before processing
     */
    CANCELLED,

    /**
     * Payment is on hold pending manual review
     */
    ON_HOLD
}
