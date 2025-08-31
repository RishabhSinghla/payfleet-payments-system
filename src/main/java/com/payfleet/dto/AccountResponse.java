package com.payfleet.dto;

import com.payfleet.model.Account;
import com.payfleet.model.AccountStatus;
import com.payfleet.model.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account Response DTO
 * <p>
 * Returns account information without sensitive internal details
 */
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private String accountName;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private String currencyCode;
    private String ownerUsername;
    private String ownerFullName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public AccountResponse() {
    }

    // Constructor from Account entity
    public AccountResponse(Account account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.accountName = account.getAccountName();
        this.accountType = account.getAccountType();
        this.balance = account.getBalance();
        this.status = account.getStatus();
        this.currencyCode = account.getCurrencyCode();
        this.ownerUsername = account.getOwner().getUsername();
        this.ownerFullName = account.getOwner().getFullName();
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOwnerFullName() {
        return ownerFullName;
    }

    public void setOwnerFullName(String ownerFullName) {
        this.ownerFullName = ownerFullName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
