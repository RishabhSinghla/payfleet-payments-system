package com.payfleet.controller;

import com.payfleet.model.AccountBalanceHistory;
import com.payfleet.service.AccountBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Account Balance Controller - APIs for balance management and tracking
 * <p>
 * Enterprise Pattern: Real-Time Balance APIs
 * Banking Context: Customer and admin balance management operations
 */
@RestController
@RequestMapping("/api/v1/accounts/balance")
public class AccountBalanceController {

    private final AccountBalanceService accountBalanceService;

    @Autowired
    public AccountBalanceController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    /**
     * Get Current Account Balance
     * <p>
     * GET /api/v1/accounts/balance/{accountNumber}
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getCurrentBalance(@PathVariable String accountNumber,
                                               Authentication authentication) {
        try {
            BigDecimal currentBalance = accountBalanceService.getCurrentBalance(accountNumber);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Current balance retrieved successfully",
                    "data", Map.of(
                            "accountNumber", accountNumber,
                            "currentBalance", currentBalance,
                            "currency", "USD",
                            "lastUpdated", LocalDateTime.now()
                    ),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve balance: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Balance History
     * <p>
     * GET /api/v1/accounts/balance/{accountNumber}/history
     */
    @GetMapping("/{accountNumber}/history")
    public ResponseEntity<?> getBalanceHistory(@PathVariable String accountNumber,
                                               @RequestParam(defaultValue = "50") int limit,
                                               Authentication authentication) {
        try {
            List<AccountBalanceHistory> history =
                    accountBalanceService.getBalanceHistory(accountNumber, limit);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Balance history retrieved successfully",
                    "data", history,
                    "count", history.size(),
                    "accountNumber", accountNumber,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve balance history: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Balance History by Date Range
     * <p>
     * GET /api/v1/accounts/balance/{accountNumber}/history/range
     */
    @GetMapping("/{accountNumber}/history/range")
    public ResponseEntity<?> getBalanceHistoryByRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {

        try {
            List<AccountBalanceHistory> history = accountBalanceService
                    .getBalanceHistoryByDateRange(accountNumber, startDate, endDate);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Balance history retrieved successfully",
                    "data", history,
                    "count", history.size(),
                    "accountNumber", accountNumber,
                    "dateRange", Map.of(
                            "startDate", startDate,
                            "endDate", endDate
                    ),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve balance history: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Balance Statistics
     * <p>
     * GET /api/v1/accounts/balance/{accountNumber}/statistics
     */
    @GetMapping("/{accountNumber}/statistics")
    public ResponseEntity<?> getBalanceStatistics(@PathVariable String accountNumber,
                                                  @RequestParam(defaultValue = "30") int days,
                                                  Authentication authentication) {
        try {
            AccountBalanceService.BalanceStatistics statistics =
                    accountBalanceService.getBalanceStatistics(accountNumber, days);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Balance statistics retrieved successfully",
                    "data", statistics,
                    "accountNumber", accountNumber,
                    "periodDays", days,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve balance statistics: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Manual Balance Adjustment (Admin Only)
     * <p>
     * PUT /api/v1/accounts/balance/{accountNumber}/adjust
     */
    @PutMapping("/{accountNumber}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adjustBalance(@PathVariable String accountNumber,
                                           @RequestParam BigDecimal amount,
                                           @RequestParam String reason,
                                           Authentication authentication) {
        try {
            String adminUserId = authentication.getName();

            accountBalanceService.adjustBalance(accountNumber, amount, reason, adminUserId);

            BigDecimal newBalance = accountBalanceService.getCurrentBalance(accountNumber);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Balance adjusted successfully",
                    "data", Map.of(
                            "accountNumber", accountNumber,
                            "adjustmentAmount", amount,
                            "newBalance", newBalance,
                            "reason", reason,
                            "adjustedBy", adminUserId
                    ),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to adjust balance: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}
