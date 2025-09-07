package com.payfleet.service;

import com.payfleet.dto.PaymentEvent;
import com.payfleet.model.Account;
import com.payfleet.model.AccountBalanceHistory;
import com.payfleet.model.BalanceChangeType;
import com.payfleet.repository.AccountBalanceHistoryRepository;
import com.payfleet.repository.AccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Account Balance Service - Manages real-time balance updates and tracking
 * <p>
 * Enterprise Pattern: Real-Time Balance Management
 * Banking Context: Handles high-frequency balance updates with consistency
 */
@Service
@Transactional
public class AccountBalanceService {

    private static final Logger logger = LoggerFactory.getLogger(AccountBalanceService.class);

    private final AccountRepository accountRepository;
    private final AccountBalanceHistoryRepository balanceHistoryRepository;
    private final EntityManager entityManager;
    private final AlertService alertService;

    @Autowired
    public AccountBalanceService(AccountRepository accountRepository,
                                 AccountBalanceHistoryRepository balanceHistoryRepository,
                                 EntityManager entityManager,
                                 AlertService alertService) {
        this.accountRepository = accountRepository;
        this.balanceHistoryRepository = balanceHistoryRepository;
        this.entityManager = entityManager;
        this.alertService = alertService;
    }

    /**
     * Process Payment Events for Balance Updates
     */
    @KafkaListener(topics = "${payfleet.kafka.topics.payment-events}", groupId = "balance-management-group")
    public void processPaymentEvent(PaymentEvent event) {
        logger.info("Processing payment event for balance update: {} - {}",
                event.getEventType(), event.getPaymentReference());

        try {
            switch (event.getEventType()) {
                case "PAYMENT_COMPLETED":
                    handlePaymentCompleted(event);
                    break;

                case "PAYMENT_FAILED":
                    handlePaymentFailed(event);
                    break;

                default:
                    logger.debug("Event type {} does not require balance update", event.getEventType());
            }

        } catch (Exception e) {
            logger.error("Failed to process payment event for balance update: {}", e.getMessage(), e);
            // In production, you might want to send to dead letter queue
        }
    }

    /**
     * Handle completed payment events
     */
    private void handlePaymentCompleted(PaymentEvent event) {
        // Debit from source account
        updateAccountBalance(
                event.getFromAccountNumber(),
                event.getAmount().negate(),
                BalanceChangeType.PAYMENT_DEBIT,
                event.getPaymentReference(),
                "Payment sent: " + event.getDescription(),
                event.getInitiatedByUsername()
        );

        // Credit to destination account
        updateAccountBalance(
                event.getToAccountNumber(),
                event.getAmount(),
                BalanceChangeType.PAYMENT_CREDIT,
                event.getPaymentReference(),
                "Payment received: " + event.getDescription(),
                event.getInitiatedByUsername()
        );
    }

    /**
     * Handle failed payment events (reverse any pending changes if needed)
     */
    private void handlePaymentFailed(PaymentEvent event) {
        logger.info("Payment failed, no balance changes needed: {}", event.getPaymentReference());
        // In a more complex system, you might need to reverse pending charges
    }

    /**
     * Update account balance with proper locking and history tracking
     */
    public void updateAccountBalance(String accountNumber, BigDecimal changeAmount,
                                     BalanceChangeType changeType, String transactionReference,
                                     String reason, String userId) {

        // Find account with pessimistic lock to prevent concurrent modifications
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isEmpty()) {
            logger.error("Account not found for balance update: {}", accountNumber);
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        Account account = accountOptional.get();

        // Apply pessimistic lock for concurrent transaction safety
        entityManager.lock(account, LockModeType.PESSIMISTIC_WRITE);
        entityManager.refresh(account);

        BigDecimal previousBalance = account.getBalance();
        BigDecimal newBalance = previousBalance.add(changeAmount);

        // Validate balance constraints
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Balance update would result in negative balance: Account={}, NewBalance={}",
                    accountNumber, newBalance);

            // For some account types, negative balance might be allowed
            // For now, we'll allow it but log a warning
        }

        // Update account balance
        account.setBalance(newBalance);
        accountRepository.save(account);

        // Record balance history
        AccountBalanceHistory historyRecord = new AccountBalanceHistory(
                account, previousBalance, newBalance, changeAmount, changeType,
                transactionReference, reason, userId
        );
        balanceHistoryRepository.save(historyRecord);

        // Check for balance alerts
        checkBalanceAlerts(account, previousBalance, newBalance);

        logger.info("Updated account balance: {} from {} to {} (change: {})",
                accountNumber, previousBalance, newBalance, changeAmount);
    }

    /**
     * Check for balance-related alerts
     */
    private void checkBalanceAlerts(Account account, BigDecimal previousBalance, BigDecimal newBalance) {
        // Low balance alert
        BigDecimal lowBalanceThreshold = new BigDecimal("100.00");
        if (newBalance.compareTo(lowBalanceThreshold) <= 0 &&
                previousBalance.compareTo(lowBalanceThreshold) > 0) {

            alertService.sendLowBalanceAlert(account, newBalance);
        }

        // Negative balance alert
        if (newBalance.compareTo(BigDecimal.ZERO) < 0 &&
                previousBalance.compareTo(BigDecimal.ZERO) >= 0) {

            alertService.sendNegativeBalanceAlert(account, newBalance);
        }

        // Large transaction alert
        BigDecimal changeAmount = newBalance.subtract(previousBalance).abs();
        BigDecimal largeTransactionThreshold = new BigDecimal("10000.00");
        if (changeAmount.compareTo(largeTransactionThreshold) > 0) {

            alertService.sendLargeTransactionAlert(account, changeAmount);
        }
    }

    /**
     * Get current account balance with real-time accuracy
     */
    public BigDecimal getCurrentBalance(String accountNumber) {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        return accountOptional.get().getBalance();
    }

    /**
     * Get account balance history
     */
    public List<AccountBalanceHistory> getBalanceHistory(String accountNumber, int limit) {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        return balanceHistoryRepository.findByAccountOrderByCreatedAtDesc(accountOptional.get(), limit);
    }

    /**
     * Get balance history within date range
     */
    public List<AccountBalanceHistory> getBalanceHistoryByDateRange(String accountNumber,
                                                                    LocalDateTime startDate,
                                                                    LocalDateTime endDate) {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        return balanceHistoryRepository.findByAccountAndCreatedAtBetweenOrderByCreatedAtDesc(
                accountOptional.get(), startDate, endDate);
    }

    /**
     * Manual balance adjustment (admin function)
     */
    public void adjustBalance(String accountNumber, BigDecimal adjustmentAmount,
                              String reason, String adminUserId) {

        logger.info("Manual balance adjustment: Account={}, Amount={}, Reason={}, Admin={}",
                accountNumber, adjustmentAmount, reason, adminUserId);

        updateAccountBalance(
                accountNumber,
                adjustmentAmount,
                BalanceChangeType.ADJUSTMENT,
                "ADJ-" + System.currentTimeMillis(),
                reason,
                adminUserId
        );
    }

    /**
     * Get balance statistics for an account
     */
    public BalanceStatistics getBalanceStatistics(String accountNumber, int days) {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);

        if (accountOptional.isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }

        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        List<AccountBalanceHistory> history = balanceHistoryRepository
                .findByAccountAndCreatedAtAfterOrderByCreatedAtAsc(accountOptional.get(), fromDate);

        if (history.isEmpty()) {
            BigDecimal currentBalance = getCurrentBalance(accountNumber);
            return new BalanceStatistics(currentBalance, currentBalance, currentBalance, 0);
        }

        BigDecimal minBalance = history.stream()
                .map(AccountBalanceHistory::getNewBalance)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxBalance = history.stream()
                .map(AccountBalanceHistory::getNewBalance)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal currentBalance = getCurrentBalance(accountNumber);

        return new BalanceStatistics(currentBalance, minBalance, maxBalance, history.size());
    }

    /**
     * Inner class for balance statistics
     */
    public record BalanceStatistics(BigDecimal currentBalance, BigDecimal minBalance, BigDecimal maxBalance,
                                    int transactionCount) {
    }
}
