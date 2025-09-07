package com.payfleet.service;

import com.payfleet.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Alert Service - Handles balance-related alerts and notifications
 * <p>
 * Enterprise Pattern: Real-Time Alerting System
 * Banking Context: Customer and admin alerts for balance events
 */
@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    public void sendLowBalanceAlert(Account account, BigDecimal currentBalance) {
        logger.warn("LOW BALANCE ALERT: Account {} has low balance: {}",
                account.getAccountNumber(), currentBalance);

        // In production, send actual notifications:
        // - Email to account holder
        // - SMS notification
        // - Push notification to mobile app
        // - Admin dashboard alert

        logAlert("LOW_BALANCE", account.getAccountNumber(),
                "Account balance is low: " + currentBalance);
    }

    public void sendNegativeBalanceAlert(Account account, BigDecimal currentBalance) {
        logger.error("NEGATIVE BALANCE ALERT: Account {} has negative balance: {}",
                account.getAccountNumber(), currentBalance);

        logAlert("NEGATIVE_BALANCE", account.getAccountNumber(),
                "Account has negative balance: " + currentBalance);
    }

    public void sendLargeTransactionAlert(Account account, BigDecimal transactionAmount) {
        logger.info("LARGE TRANSACTION ALERT: Account {} had large transaction: {}",
                account.getAccountNumber(), transactionAmount);

        logAlert("LARGE_TRANSACTION", account.getAccountNumber(),
                "Large transaction processed: " + transactionAmount);
    }

    private void logAlert(String alertType, String accountNumber, String message) {
        // In production, store alerts in database for tracking
        logger.info("ALERT [{}] Account: {} - {}", alertType, accountNumber, message);
    }
}
