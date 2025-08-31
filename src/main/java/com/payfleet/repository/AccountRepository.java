package com.payfleet.repository;

import com.payfleet.model.Account;
import com.payfleet.model.AccountStatus;
import com.payfleet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Account Repository - Database operations for Account entity
 * <p>
 * Enterprise Pattern: Repository for account management
 * Provides query methods for account operations and reporting
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Find all accounts belonging to a specific user
     */
    List<Account> findByOwner(User owner);

    /**
     * Find all active accounts for a user
     */
    List<Account> findByOwnerAndStatus(User owner, AccountStatus status);

    /**
     * Check if account number already exists
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Count total accounts for a user
     */
    long countByOwner(User owner);

    /**
     * Find accounts by status
     */
    List<Account> findByStatus(AccountStatus status);

    /**
     * Custom query to find accounts with balance greater than specified amount
     */
    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance AND a.status = :status")
    List<Account> findAccountsWithMinimumBalance(@Param("minBalance") java.math.BigDecimal minBalance,
                                                 @Param("status") AccountStatus status);
}
