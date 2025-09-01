package com.payfleet.exception;

/**
 * Exception thrown when a requested account is not found
 */
public class AccountNotFoundException extends PayFleetException {

    private final String accountNumber;

    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber, "ACCOUNT_NOT_FOUND");
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
