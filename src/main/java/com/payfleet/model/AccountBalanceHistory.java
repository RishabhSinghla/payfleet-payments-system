package com.payfleet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account Balance History Entity - Tracks all balance changes over time
 * <p>
 * Enterprise Pattern: Balance Audit Trail
 * Banking Context: Complete balance history for regulatory compliance and analytics
 */
@Entity
@Table(name = "account_balance_history",
        indexes = {
                @Index(name = "idx_account_id", columnList = "account_id"),
                @Index(name = "idx_transaction_ref", columnList = "transaction_reference"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_balance_type", columnList = "balance_type")
        })
public class AccountBalanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Account - Reference to the account
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Previous Balance - Balance before the change
     */
    @Column(name = "previous_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal previousBalance;

    /**
     * New Balance - Balance after the change
     */
    @Column(name = "new_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal newBalance;

    /**
     * Change Amount - Amount of the change (positive for credit, negative for debit)
     */
    @Column(name = "change_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal changeAmount;

    /**
     * Balance Type - Type of balance change
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "balance_type", nullable = false)
    private BalanceChangeType balanceType;

    /**
     * Transaction Reference - Reference to the transaction that caused this change
     */
    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    /**
     * Change Reason - Reason for the balance change
     */
    @Column(name = "change_reason", length = 500)
    private String changeReason;

    /**
     * User ID - Who initiated the change
     */
    @Column(name = "user_id", length = 50)
    private String userId;

    /**
     * Created timestamp
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Default Constructor
    public AccountBalanceHistory() {
    }

    // Constructor for balance changes
    public AccountBalanceHistory(Account account, BigDecimal previousBalance, BigDecimal newBalance,
                                 BigDecimal changeAmount, BalanceChangeType balanceType,
                                 String transactionReference, String changeReason, String userId) {
        this.account = account;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
        this.changeAmount = changeAmount;
        this.balanceType = balanceType;
        this.transactionReference = transactionReference;
        this.changeReason = changeReason;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public BalanceChangeType getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(BalanceChangeType balanceType) {
        this.balanceType = balanceType;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
