package com.payfleet.repository;

import com.payfleet.model.Account;
import com.payfleet.model.AccountBalanceHistory;
import com.payfleet.model.BalanceChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Account Balance History Repository - Database operations for balance history
 */
@Repository
public interface AccountBalanceHistoryRepository extends JpaRepository<AccountBalanceHistory, Long> {

    /**
     * Find balance history for account (most recent first)
     */
    @Query("SELECT h FROM AccountBalanceHistory h WHERE h.account = :account ORDER BY h.createdAt DESC")
    List<AccountBalanceHistory> findByAccountOrderByCreatedAtDesc(@Param("account") Account account,
                                                                  @Param("limit") int limit);

    /**
     * Find balance history within date range
     */
    List<AccountBalanceHistory> findByAccountAndCreatedAtBetweenOrderByCreatedAtDesc(
            Account account, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find balance history after specific date
     */
    List<AccountBalanceHistory> findByAccountAndCreatedAtAfterOrderByCreatedAtAsc(
            Account account, LocalDateTime fromDate);

    /**
     * Find balance history by transaction reference
     */
    List<AccountBalanceHistory> findByTransactionReference(String transactionReference);

    /**
     * Find balance history by change type
     */
    List<AccountBalanceHistory> findByAccountAndBalanceTypeOrderByCreatedAtDesc(
            Account account, BalanceChangeType balanceType);

    /**
     * Find balance history by user
     */
    List<AccountBalanceHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Get balance history count for account
     */
    long countByAccount(Account account);

    /**
     * Custom query to get balance summary for account
     */
    @Query("SELECT h FROM AccountBalanceHistory h WHERE h.account = :account " +
            "AND h.createdAt >= :fromDate ORDER BY h.createdAt DESC")
    List<AccountBalanceHistory> findRecentBalanceHistory(@Param("account") Account account,
                                                         @Param("fromDate") LocalDateTime fromDate);
}
