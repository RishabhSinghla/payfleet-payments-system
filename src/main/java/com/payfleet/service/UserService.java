package com.payfleet.service;

import com.payfleet.dto.UserRegistrationRequest;
import com.payfleet.dto.UserResponse;
import com.payfleet.model.User;
import com.payfleet.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * UserService - Business Logic Layer for User Operations
 * <p>
 * Enterprise Pattern: Service Layer Pattern
 * - Handles business rules and validation
 * - Manages transactions
 * - Coordinates between controllers and repositories
 * - Encrypts sensitive data before storage
 * <p>
 * Banking Context: User management is critical for financial systems
 * - Must prevent duplicate accounts
 * - Must securely store passwords
 * - Must validate all input data
 * - Must maintain audit trails
 */
@Service
@Transactional  // Ensures all database operations are atomic
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor injection - preferred over field injection
     * Makes dependencies explicit and enables easier testing
     */
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Register New User - Core Business Operation
     * <p>
     * Business Rules:
     * 1. Username must be unique across all users
     * 2. Email must be unique across all users
     * 3. Password must be encrypted before storage
     * 4. User gets default role of USER
     * 5. User status defaults to ACTIVE
     *
     * @param request Validated registration data from controller
     * @return UserResponse DTO (without password for security)
     * @throws IllegalArgumentException if business rules are violated
     */
    public UserResponse registerUser(UserRegistrationRequest request) {

        // Business Rule 1: Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username '" + request.getUsername() + "' is already taken. Please choose another.");
        }

        // Business Rule 2: Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email '" + request.getEmail() + "' is already registered. Please use another email.");
        }

        // Business Rule 3: Encrypt password before storage
        // BCrypt automatically generates salt and handles security
        String encryptedPassword = passwordEncoder.encode(request.getPassword());

        // Create new user entity with encrypted password
        User newUser = new User(
                request.getUsername(),
                request.getEmail(),
                encryptedPassword,  // Never store plain text passwords!
                request.getFirstName(),
                request.getLastName()
        );

        // Save to database (JPA handles SQL generation)
        User savedUser = userRepository.save(newUser);

        // Return response DTO (excludes password for security)
        return new UserResponse(savedUser);
    }

    /**
     * Find User by Username - Used for Authentication
     *
     * @param username The username to search for
     * @return Optional<User> - empty if not found, populated if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Find User by Email - Used for Password Reset, etc.
     *
     * @param email The email to search for
     * @return Optional<User> - empty if not found, populated if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Verify Password - Used for Login Authentication
     *
     * @param rawPassword     Plain text password from login form
     * @param encodedPassword Encrypted password from database
     * @return true if passwords match, false otherwise
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
