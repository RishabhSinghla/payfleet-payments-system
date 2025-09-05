package com.payfleet.dto;

/**
 * Payment Event Types - Different types of payment events
 */
public class PaymentEventType {
    public static final String PAYMENT_INITIATED = "PAYMENT_INITIATED";
    public static final String PAYMENT_PROCESSING = "PAYMENT_PROCESSING";
    public static final String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String PAYMENT_CANCELLED = "PAYMENT_CANCELLED";
    public static final String ACCOUNT_DEBITED = "ACCOUNT_DEBITED";
    public static final String ACCOUNT_CREDITED = "ACCOUNT_CREDITED";
}
