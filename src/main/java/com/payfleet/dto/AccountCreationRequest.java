package com.payfleet.dto;

import com.payfleet.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Account Creation Request DTO
 * <p>
 * Captures information needed to create a new bank account
 */
public class AccountCreationRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode = "USD";

    // Constructors
    public AccountCreationRequest() {
    }

    public AccountCreationRequest(String accountName, AccountType accountType, String currencyCode) {
        this.accountName = accountName;
        this.accountType = accountType;
        this.currencyCode = currencyCode;
    }

    // Getters and Setters
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
