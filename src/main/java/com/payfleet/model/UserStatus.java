package com.payfleet.model;

/**
 * User Account Status - Controls account accessibility
 * <p>
 * Banking Requirement: Must be able to freeze suspicious accounts
 */
public enum UserStatus {
    /**
     * Account is active and fully functional
     */
    ACTIVE,

    /**
     * Account is temporarily suspended
     */
    SUSPENDED,

    /**
     * Account is permanently deactivated
     */
    INACTIVE,

    /**
     * Account is pending verification
     */
    PENDING_VERIFICATION
}
