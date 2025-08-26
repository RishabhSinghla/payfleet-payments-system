package com.payfleet.repository;

import com.payfleet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Database Access Layer for User Operations
 * <p>
 * Enterprise Pattern: Repository Pattern with Spring Data JPA
 * - Provides CRUD operations automatically
 * - Custom query methods by method naming convention
 * - Type-safe database operations
 * <p>
 * JPMC Relevance:
 * - All enterprise apps use repository pattern
 * - Separates business logic from data access
 * - Enables easy testing with mocks
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username (for login authentication)
     * Spring Data JPA automatically implements this method
     * Translates to: SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email (for email-based operations)
     * Translates to: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username already exists (for registration validation)
     * Translates to: SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)
     * Returns true/false instead of full object (more efficient)
     */
    boolean existsByUsername(String username);

    /**
     * Check if email already exists (for registration validation)
     * Translates to: SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     */
    boolean existsByEmail(String email);
}
