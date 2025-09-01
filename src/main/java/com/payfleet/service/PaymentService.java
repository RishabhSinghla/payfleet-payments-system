package com.payfleet.service;

import com.payfleet.dto.PaymentInitiationRequest;
import com.payfleet.dto.PaymentResponse;
import com.payfleet.exception.AccountNotFoundException;
import com.payfleet.exception.DuplicatePaymentException;
import com.payfleet.exception.InsufficientFundsException;
import com.payfleet.exception.InvalidPaymentException;
import com.payfleet.model.Account;
import com.payfleet.model.Payment;
import com.payfleet.model.PaymentStatus;
import com.payfleet.model.User;
import com.payfleet.repository.AccountRepository;
import com.payfleet.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Payment Service - Core business logic for payment processing
 * <p>
 * Enterprise Banking Operations:
 * - Payment initiation and validation
 * - Account balance verification and updates
 * - Transaction processing with rollback capabilities
 * - Idempotency handling for reliable processing
 * - Comprehensive business rule enforcement
 * <p>
 * Banking Context: Heart of the payment system
 */
@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final UserService userService;
    private final Random random = new Random();

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          AccountRepository accountRepository,
                          UserService userService) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    /**
     * Initiate Payment Transaction
     * <p>
     * Business Rules:
     * 1. Validate source and destination accounts exist and are active
     * 2. Verify sufficient balance in source account
     * 3. Check for duplicate transactions (idempotency)
     * 4. Validate user has permission to debit source account
     * 5. Create payment record and update account balances
     *
     * @param request           Payment initiation details
     * @param initiatorUsername Username of payment initiator
     * @return PaymentResponse DTO
     */
    public PaymentResponse initiatePayment(PaymentInitiationRequest request, String initiatorUsername) {

        // Step 1: Validate initiator
        Optional<User> initiatorOptional = userService.findByUsername(initiatorUsername);
        if (initiatorOptional.isEmpty()) {
            throw new InvalidPaymentException("Initiator user not found: " + initiatorUsername);
        }
        User initiator = initiatorOptional.get();

        // Step 2: Check for duplicate transaction (idempotency)
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isEmpty()) {
            Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingPayment.isPresent()) {
                throw new DuplicatePaymentException(
                        request.getIdempotencyKey(),
                        existingPayment.get().getPaymentReference()
                );
            }
        }

        // Step 3: Validate and retrieve source account
        Account fromAccount = validateAndGetAccount(request.getFromAccountNumber());

        // Step 4: Validate and retrieve destination account
        Account toAccount = validateAndGetAccount(request.getToAccountNumber());

        // Step 5: Business rule validations
        validatePaymentRequest(request, fromAccount, toAccount, initiator);

        // Step 6: Generate unique payment reference
        String paymentReference = generateUniquePaymentReference();

        // Step 7: Create payment record
        Payment payment = new Payment(
                paymentReference,
                fromAccount,
                toAccount,
                request.getAmount(),
                request.getCurrency(),
                request.getDescription(),
                initiator
        );

        if (request.getIdempotencyKey() != null) {
            payment.setIdempotencyKey(request.getIdempotencyKey());
        }

        try {
            // Step 8: Process the payment (debit source, credit destination)
            processPaymentTransaction(payment);

            // Step 9: Save payment record
            Payment savedPayment = paymentRepository.save(payment);

            return new PaymentResponse(savedPayment);

        } catch (Exception e) {
            // Mark payment as failed and save
            payment.markAsFailed("Payment processing failed: " + e.getMessage());
            paymentRepository.save(payment);
            throw new RuntimeException("Payment failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate payment request against business rules
     */
    private void validatePaymentRequest(PaymentInitiationRequest request, Account fromAccount,
                                        Account toAccount, User initiator) {

        // Rule 1: Cannot send money to the same account
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new InvalidPaymentException("Cannot transfer money to the same account");
        }

        // Rule 2: User must own the source account
        if (!fromAccount.getOwner().getId().equals(initiator.getId())) {
            throw new InvalidPaymentException("User does not have permission to debit this account");
        }

        // Rule 3: Source account must be active
        if (!fromAccount.isActive()) {
            throw new InvalidPaymentException("Source account is not active");
        }

        // Rule 4: Destination account must be active
        if (!toAccount.isActive()) {
            throw new InvalidPaymentException("Destination account is not active");
        }

        // Rule 5: Check sufficient balance
        if (!fromAccount.hasSufficientBalance(request.getAmount())) {
            throw new InsufficientFundsException(
                    fromAccount.getAccountNumber(),
                    request.getAmount(),
                    fromAccount.getBalance()
            );
        }

        // Rule 6: Currency validation (simplified - both accounts should support the currency)
        if (!fromAccount.getCurrencyCode().equals(request.getCurrency()) ||
                !toAccount.getCurrencyCode().equals(request.getCurrency())) {
            throw new InvalidPaymentException("Currency mismatch between accounts and payment");
        }


        // Rule 7: Amount validation
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentException("Payment amount must be positive");
        }

        // Rule 8: Maximum payment limit (business rule)
        BigDecimal maxPaymentLimit = new BigDecimal("100000.00"); // $100,000 limit
        if (request.getAmount().compareTo(maxPaymentLimit) > 0) {
            throw new InvalidPaymentException("Payment amount exceeds maximum limit of " + maxPaymentLimit);
        }
    }

    /**
     * Process the actual payment transaction (debit/credit accounts)
     */
    private void processPaymentTransaction(Payment payment) {
        try {
            Account fromAccount = payment.getFromAccount();
            Account toAccount = payment.getToAccount();
            BigDecimal amount = payment.getAmount();

            // Debit source account
            fromAccount.debit(amount);
            accountRepository.save(fromAccount);

            // Credit destination account
            toAccount.credit(amount);
            accountRepository.save(toAccount);

            // Mark payment as completed
            payment.markAsCompleted();
            payment.setProcessingDetails("Payment processed successfully at " + LocalDateTime.now());

        } catch (Exception e) {
            // If anything fails, the @Transactional annotation will rollback all changes
            throw new RuntimeException("Transaction processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate and retrieve account by account number
     */
    private Account validateAndGetAccount(String accountNumber) {
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isEmpty()) {
            throw new AccountNotFoundException(accountNumber);
        }
        return accountOptional.get();
    }

    /**
     * Generate unique payment reference
     * Format: PAY-YYYYMMDD-XXXXXX
     */
    private String generateUniquePaymentReference() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String paymentReference;

        do {
            int randomNumber = 100000 + random.nextInt(900000);
            paymentReference = "PAY-" + datePrefix + "-" + randomNumber;
        } while (paymentRepository.findByPaymentReference(paymentReference).isPresent());

        return paymentReference;
    }

    /**
     * Get payment by reference
     */
    public Optional<PaymentResponse> getPaymentByReference(String paymentReference) {
        return paymentRepository.findByPaymentReference(paymentReference)
                .map(PaymentResponse::new);
    }

    /**
     * Get payment by ID
     */
    public Optional<PaymentResponse> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentResponse::new);
    }

    /**
     * Get all payments initiated by a user
     */
    public List<PaymentResponse> getUserPayments(String username) {
        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new InvalidPaymentException("User not found: " + username);
        }

        List<Payment> payments = paymentRepository.findByInitiatedBy(userOptional.get());
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get payment history for an account
     */
    public List<PaymentResponse> getAccountPaymentHistory(String accountNumber) {
        Account account = validateAndGetAccount(accountNumber);
        List<Payment> payments = paymentRepository.findPaymentsInvolvingAccount(account);
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Cancel payment (if still pending)
     */
    public PaymentResponse cancelPayment(Long paymentId, String username) {
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        if (paymentOptional.isEmpty()) {
            throw new IllegalArgumentException("Payment not found");
        }

        Payment payment = paymentOptional.get();

        // Verify user owns the payment
        if (!payment.getInitiatedBy().getUsername().equals(username)) {
            throw new IllegalArgumentException("User does not have permission to cancel this payment");
        }

        // Can only cancel pending payments
        if (!payment.isPending()) {
            throw new IllegalArgumentException("Cannot cancel payment in " + payment.getStatus() + " status");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(savedPayment);
    }
}
