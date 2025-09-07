package com.payfleet.model;

/**
 * Balance Change Type - Different types of balance changes
 */
public enum BalanceChangeType {
    /**
     * Payment debit - Money sent to another account
     */
    PAYMENT_DEBIT,

    /**
     * Payment credit - Money received from another account
     */
    PAYMENT_CREDIT,

    /**
     * Manual credit - Admin or user credited account
     */
    MANUAL_CREDIT,

    /**
     * Manual debit - Admin or user debited account
     */
    MANUAL_DEBIT,

    /**
     * Interest credit - Interest earned on account
     */
    INTEREST_CREDIT,

    /**
     * Fee debit - Bank fees or charges
     */
    FEE_DEBIT,

    /**
     * Adjustment - Balance adjustment/correction
     */
    ADJUSTMENT,

    /**
     * Initial balance - Account opening balance
     */
    INITIAL_BALANCE
}
