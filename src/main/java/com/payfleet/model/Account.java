package com.payfleet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account Entity - Represents bank accounts in the payment system
 * <p>
 * Enterprise Pattern: Core Banking Domain Model
 * - Each user can have multiple accounts
 * - Account numbers are unique system-wide
 * - Balance tracking with precision for financial calculations
 * - Account status management for security controls
 * <p>
 * Banking Context: Foundation of all financial operations
 */
@Entity
@Table(name = "accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = "account_number"))
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Account Number - Unique identifier for the account
     * Format: ACC-YYYYMMDD-XXXXXX (e.g., ACC-20250831-123456)
     */
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    @NotBlank(message = "Account number is required")
    @Size(max = 20, message = "Account number must not exceed 20 characters")
    private String accountNumber;

    /**
     * Account Name - Display name for the account
     */
    @Column(name = "account_name", nullable = false, length = 100)
    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    /**
     * Account Type - Different types of accounts
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    /**
     * Current Balance - Account balance with precision for financial calculations
     * Using BigDecimal for exact decimal arithmetic (no floating point errors)
     */
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Account Status - Controls account accessibility
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * Account Owner - Many-to-One relationship with User
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Account owner is required")
    private User owner;

    /**
     * Currency Code - ISO currency code (USD, EUR, INR, etc.)
     */
    @Column(name = "currency_code", nullable = false, length = 3)
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    private String currencyCode = "USD";

    /**
     * Audit Fields - Track record creation and modification
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default Constructor
    public Account() {
    }

    // Constructor for creating new accounts
    public Account(String accountNumber, String accountName, AccountType accountType,
                   User owner, String currencyCode) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.accountType = accountType;
        this.owner = owner;
        this.currencyCode = currencyCode;
        this.balance = BigDecimal.ZERO;
        this.status = AccountStatus.ACTIVE;
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Business Logic Methods

    /**
     * Check if account is active and can be used for transactions
     */
    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    /**
     * Add money to account balance
     */
    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
        }
    }

    /**
     * Subtract money from account balance
     */
    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0 &&
                this.balance.compareTo(amount) >= 0) {
            this.balance = this.balance.subtract(amount);
        } else {
            throw new IllegalArgumentException("Insufficient balance");
        }
    }

    /**
     * Check if account has sufficient balance for a transaction
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
}
