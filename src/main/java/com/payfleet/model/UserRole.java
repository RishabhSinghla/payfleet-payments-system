package com.payfleet.model;

/**
 * User Roles - Defines access levels in the system
 * <p>
 * Enterprise Pattern: Role-based Access Control (RBAC)
 * Banking Context: Different users need different permissions
 */
public enum UserRole {
    /**
     * Regular user - Can manage own accounts and make payments
     */
    USER,

    /**
     * Administrator - Can manage all users and system settings
     */
    ADMIN
}
