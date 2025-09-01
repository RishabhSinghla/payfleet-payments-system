package com.payfleet.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when account has insufficient funds for a transaction
 */
public class InsufficientFundsException extends PayFleetException {

    private final String accountNumber;
    private final BigDecimal requestedAmount;
    private final BigDecimal availableBalance;

    public InsufficientFundsException(String accountNumber, BigDecimal requestedAmount,
                                      BigDecimal availableBalance) {
        super(String.format("Insufficient funds in account %s. Requested: %s, Available: %s",
                        accountNumber, requestedAmount, availableBalance),
                "INSUFFICIENT_FUNDS");
        this.accountNumber = accountNumber;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
}
