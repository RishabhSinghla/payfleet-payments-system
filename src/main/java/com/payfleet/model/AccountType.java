package com.payfleet.model;

/**
 * Account Types - Different categories of bank accounts
 * <p>
 * Banking Context: Different account types have different rules and features
 */
public enum AccountType {
    /**
     * Checking Account - Primary account for daily transactions
     */
    CHECKING,

    /**
     * Savings Account - Interest-bearing account with withdrawal limits
     */
    SAVINGS,

    /**
     * Business Account - Account for business transactions
     */
    BUSINESS,

    /**
     * Investment Account - Account for investment-related transactions
     */
    INVESTMENT
}
