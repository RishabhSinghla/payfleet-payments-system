package com.payfleet.model;

/**
 * Account Status - Controls account accessibility and operations
 * <p>
 * Banking Requirement: Must be able to control account access for security
 */
public enum AccountStatus {
    /**
     * Account is active and fully operational
     */
    ACTIVE,

    /**
     * Account is temporarily frozen (no transactions allowed)
     */
    FROZEN,

    /**
     * Account is suspended pending investigation
     */
    SUSPENDED,

    /**
     * Account is permanently closed
     */
    CLOSED,

    /**
     * Account is pending activation (newly created)
     */
    PENDING_ACTIVATION
}
