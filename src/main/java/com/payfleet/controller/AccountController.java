package com.payfleet.controller;

import com.payfleet.dto.AccountCreationRequest;
import com.payfleet.dto.AccountResponse;
import com.payfleet.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Account Controller - REST API for bank account management
 * <p>
 * Enterprise Banking APIs:
 * - Account creation and management
 * - Balance inquiries and updates
 * - Account status controls
 * - Security through JWT authentication
 * <p>
 * Banking Context: Core APIs for customer account operations
 */
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Create New Bank Account
     * <p>
     * POST /api/v1/accounts
     * <p>
     * Creates a new bank account for the authenticated user
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountCreationRequest request,
                                           Authentication authentication) {
        try {
            String username = authentication.getName();
            AccountResponse account = accountService.createAccount(request, username);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Account created successfully",
                    "data", account,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get All User Accounts
     * <p>
     * GET /api/v1/accounts/my-accounts
     * <p>
     * Returns all accounts belonging to the authenticated user
     */
    @GetMapping("/my-accounts")
    public ResponseEntity<?> getMyAccounts(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<AccountResponse> accounts = accountService.getUserAccounts(username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Accounts retrieved successfully",
                    "data", accounts,
                    "count", accounts.size(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Account Details
     * <p>
     * GET /api/v1/accounts/{accountId}
     * <p>
     * Returns details of a specific account (owner verification required)
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccount(@PathVariable Long accountId,
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<AccountResponse> accountOptional =
                    accountService.getAccountById(accountId, username);

            if (accountOptional.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Account details retrieved successfully",
                        "data", accountOptional.get(),
                        "timestamp", LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Account not found",
                        "timestamp", LocalDateTime.now()
                ));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve account details",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Account Balance
     * <p>
     * GET /api/v1/accounts/{accountId}/balance
     * <p>
     * Returns current balance of the specified account
     */
    @GetMapping("/{accountId}/balance")
    public ResponseEntity<?> getAccountBalance(@PathVariable Long accountId,
                                               Authentication authentication) {
        try {
            String username = authentication.getName();
            BigDecimal balance = accountService.getAccountBalance(accountId, username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Balance retrieved successfully",
                    "data", Map.of(
                            "accountId", accountId,
                            "balance", balance,
                            "currency", "USD"
                    ),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve balance",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Freeze Account
     * <p>
     * PUT /api/v1/accounts/{accountId}/freeze
     * <p>
     * Freezes the account (suspends all transactions)
     */
    @PutMapping("/{accountId}/freeze")
    public ResponseEntity<?> freezeAccount(@PathVariable Long accountId,
                                           Authentication authentication) {
        try {
            String username = authentication.getName();
            AccountResponse account = accountService.freezeAccount(accountId, username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account frozen successfully",
                    "data", account,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to freeze account",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Unfreeze Account
     * <p>
     * PUT /api/v1/accounts/{accountId}/unfreeze
     * <p>
     * Unfreezes the account (restores normal operations)
     */
    @PutMapping("/{accountId}/unfreeze")
    public ResponseEntity<?> unfreezeAccount(@PathVariable Long accountId,
                                             Authentication authentication) {
        try {
            String username = authentication.getName();
            AccountResponse account = accountService.unfreezeAccount(accountId, username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account unfrozen successfully",
                    "data", account,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to unfreeze account",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Credit Account (Add Money) - Demo/Testing Purpose
     * <p>
     * PUT /api/v1/accounts/{accountId}/credit
     * <p>
     * Adds money to the account balance
     */
    @PutMapping("/{accountId}/credit")
    public ResponseEntity<?> creditAccount(@PathVariable Long accountId,
                                           @RequestParam BigDecimal amount,
                                           Authentication authentication) {
        try {
            String username = authentication.getName();
            AccountResponse account = accountService.creditAccount(accountId, amount, username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account credited successfully",
                    "data", account,
                    "creditAmount", amount,
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
                    "message", "Failed to credit account",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Admin Only - Get All Accounts
     * <p>
     * GET /api/v1/accounts/admin/all
     * <p>
     * Returns all accounts in the system (admin only)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAccounts(Authentication authentication) {
        try {
            List<AccountResponse> accounts = accountService.getAllAccounts();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "All accounts retrieved successfully",
                    "data", accounts,
                    "count", accounts.size(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve accounts",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}
