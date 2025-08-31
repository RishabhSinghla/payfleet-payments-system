package com.payfleet.service;

import com.payfleet.dto.AccountCreationRequest;
import com.payfleet.dto.AccountResponse;
import com.payfleet.model.Account;
import com.payfleet.model.AccountStatus;
import com.payfleet.model.User;
import com.payfleet.repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Account Service - Business logic for account management
 * <p>
 * Enterprise Banking Operations:
 * - Account creation with unique account numbers
 * - Balance management and validation
 * - Account status controls (freeze/unfreeze)
 * - Account ownership verification
 * <p>
 * Banking Context: Core service for all account-related operations
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;
    private final Random random = new Random();

    @Autowired
    public AccountService(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    /**
     * Create New Bank Account
     * <p>
     * Business Rules:
     * 1. Generate unique account number
     * 2. Validate account creation request
     * 3. Set initial balance to zero
     * 4. Account starts in ACTIVE status
     *
     * @param request       Account creation details
     * @param ownerUsername Username of account owner
     * @return AccountResponse DTO
     */
    public AccountResponse createAccount(AccountCreationRequest request, String ownerUsername) {
        // Find account owner
        Optional<User> ownerOptional = userService.findByUsername(ownerUsername);
        if (ownerOptional.isEmpty()) {
            throw new IllegalArgumentException("Account owner not found: " + ownerUsername);
        }

        User owner = ownerOptional.get();

        // Generate unique account number
        String accountNumber = generateUniqueAccountNumber();

        // Create new account
        Account account = new Account(
                accountNumber,
                request.getAccountName(),
                request.getAccountType(),
                owner,
                request.getCurrencyCode()
        );

        // Save account
        Account savedAccount = accountRepository.save(account);

        return new AccountResponse(savedAccount);
    }

    /**
     * Generate Unique Account Number
     * Format: ACC-YYYYMMDD-XXXXXX
     * Example: ACC-20250831-123456
     */
    private String generateUniqueAccountNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String accountNumber;

        do {
            // Generate 6-digit random number
            int randomNumber = 100000 + random.nextInt(900000);
            accountNumber = "ACC-" + datePrefix + "-" + randomNumber;
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    /**
     * Get All Accounts for User
     */
    public List<AccountResponse> getUserAccounts(String username) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + username);
        }

        List<Account> accounts = accountRepository.findByOwner(userOptional.get());
        return accounts.stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get Account by Account Number
     */
    public Optional<AccountResponse> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(AccountResponse::new);
    }

    /**
     * Get Account by ID (with ownership verification)
     */
    public Optional<AccountResponse> getAccountById(Long accountId, String ownerUsername) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            // Verify ownership
            if (account.getOwner().getUsername().equals(ownerUsername)) {
                return Optional.of(new AccountResponse(account));
            } else {
                throw new IllegalArgumentException("Access denied: Account does not belong to user");
            }
        }

        return Optional.empty();
    }

    /**
     * Freeze Account (suspend all transactions)
     */
    public AccountResponse freezeAccount(Long accountId, String ownerUsername) {
        Account account = getAccountEntity(accountId, ownerUsername);
        account.setStatus(AccountStatus.FROZEN);
        Account savedAccount = accountRepository.save(account);
        return new AccountResponse(savedAccount);
    }

    /**
     * Unfreeze Account (restore normal operations)
     */
    public AccountResponse unfreezeAccount(Long accountId, String ownerUsername) {
        Account account = getAccountEntity(accountId, ownerUsername);
        account.setStatus(AccountStatus.ACTIVE);
        Account savedAccount = accountRepository.save(account);
        return new AccountResponse(savedAccount);
    }

    /**
     * Get Account Balance
     */
    public BigDecimal getAccountBalance(Long accountId, String ownerUsername) {
        Account account = getAccountEntity(accountId, ownerUsername);
        return account.getBalance();
    }

    /**
     * Update Account Balance (internal method for payment processing)
     */
    public void updateAccountBalance(String accountNumber, BigDecimal newBalance) {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setBalance(newBalance);
            accountRepository.save(account);
        } else {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
    }

    /**
     * Credit Account (add money)
     */
    public AccountResponse creditAccount(Long accountId, BigDecimal amount, String ownerUsername) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }

        Account account = getAccountEntity(accountId, ownerUsername);

        if (!account.isActive()) {
            throw new IllegalArgumentException("Cannot credit inactive account");
        }

        account.credit(amount);
        Account savedAccount = accountRepository.save(account);
        return new AccountResponse(savedAccount);
    }

    /**
     * Debit Account (subtract money)
     */
    public AccountResponse debitAccount(Long accountId, BigDecimal amount, String ownerUsername) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }

        Account account = getAccountEntity(accountId, ownerUsername);

        if (!account.isActive()) {
            throw new IllegalArgumentException("Cannot debit inactive account");
        }

        if (!account.hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Insufficient account balance");
        }

        account.debit(amount);
        Account savedAccount = accountRepository.save(account);
        return new AccountResponse(savedAccount);
    }

    /**
     * Get Account Entity with Ownership Verification
     */
    private Account getAccountEntity(Long accountId, String ownerUsername) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);

        if (accountOptional.isEmpty()) {
            throw new IllegalArgumentException("Account not found");
        }

        Account account = accountOptional.get();

        // Verify ownership
        if (!account.getOwner().getUsername().equals(ownerUsername)) {
            throw new IllegalArgumentException("Access denied: Account does not belong to user");
        }

        return account;
    }

    /**
     * Admin Function - Get All Accounts
     */
    public List<AccountResponse> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(AccountResponse::new)
                .collect(Collectors.toList());
    }
}
