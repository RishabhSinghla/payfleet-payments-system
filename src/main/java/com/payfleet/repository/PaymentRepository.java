package com.payfleet.repository;

import com.payfleet.model.Account;
import com.payfleet.model.Payment;
import com.payfleet.model.PaymentStatus;
import com.payfleet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Repository - Database operations for Payment entity
 * <p>
 * Enterprise Pattern: Repository for payment transaction management
 * Provides query methods for payment operations and reporting
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by payment reference
     */
    Optional<Payment> findByPaymentReference(String paymentReference);

    /**
     * Find payment by idempotency key (prevents duplicate processing)
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find all payments initiated by a user
     */
    List<Payment> findByInitiatedBy(User user);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find payments from a specific account
     */
    List<Payment> findByFromAccount(Account account);

    /**
     * Find payments to a specific account
     */
    List<Payment> findByToAccount(Account account);

    /**
     * Find all payments involving an account (sent or received)
     */
    @Query("SELECT p FROM Payment p WHERE p.fromAccount = :account OR p.toAccount = :account")
    List<Payment> findPaymentsInvolvingAccount(@Param("account") Account account);

    /**
     * Find payments by user and status
     */
    List<Payment> findByInitiatedByAndStatus(User user, PaymentStatus status);

    /**
     * Find payments created within a date range
     */
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find payments above a certain amount
     */
    List<Payment> findByAmountGreaterThan(BigDecimal amount);

    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);

    /**
     * Custom query to find pending payments older than specified time
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :cutoffTime")
    List<Payment> findStalePayments(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get payment statistics for a user
     */
    @Query("SELECT p.status, COUNT(p), SUM(p.amount) FROM Payment p WHERE p.initiatedBy = :user GROUP BY p.status")
    List<Object[]> getPaymentStatisticsByUser(@Param("user") User user);
}
