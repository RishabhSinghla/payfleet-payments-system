package com.payfleet.controller;

import com.payfleet.dto.UserRegistrationRequest;
import com.payfleet.dto.UserResponse;
import com.payfleet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * UserController - REST API Layer for User Operations
 * <p>
 * Enterprise Pattern: Controller Layer in MVC Architecture
 * - Handles HTTP requests and responses
 * - Validates input using Bean Validation (@Valid)
 * - Maps business exceptions to HTTP status codes
 * - Returns consistent JSON response format
 * <p>
 * JPMC Relevance: All customer-facing APIs follow this pattern
 * - Clean separation of concerns
 * - Standardized error handling
 * - Proper HTTP status codes
 * - Input validation at API boundary
 */
@RestController
@RequestMapping("/api/v1/users")  // Base path for all user operations
public class UserController {

    private final UserService userService;

    /**
     * Constructor injection of UserService
     * Controller depends on Service layer for business operations
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * User Registration Endpoint
     * <p>
     * POST /payfleet/api/v1/users/register
     * <p>
     * Accepts JSON request body with user registration data
     * Validates input using Bean Validation annotations
     * Returns created user data or error message
     *
     * @param request UserRegistrationRequest DTO with validation annotations
     * @return ResponseEntity with UserResponse or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            // Call service layer to handle business logic
            UserResponse response = userService.registerUser(request);

            // Return success response with HTTP 201 Created
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "User registered successfully",
                    "data", response,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            // Business rule violation - return HTTP 400 Bad Request
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            // Unexpected error - return HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Registration failed due to server error",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Check Username Availability
     * <p>
     * GET /payfleet/api/v1/users/check-username?username=someuser
     * <p>
     * Allows frontend to check username availability before form submission
     * Improves user experience by providing immediate feedback
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = userService.findByUsername(username).isEmpty();

        return ResponseEntity.ok(Map.of(
                "username", username,
                "available", isAvailable,
                "message", isAvailable ? "Username is available" : "Username is already taken",
                "timestamp", LocalDateTime.now()
        ));
    }

    /**
     * Check Email Availability
     * <p>
     * GET /payfleet/api/v1/users/check-email?email=user@example.com
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = userService.findByEmail(email).isEmpty();

        return ResponseEntity.ok(Map.of(
                "email", email,
                "available", isAvailable,
                "message", isAvailable ? "Email is available" : "Email is already registered",
                "timestamp", LocalDateTime.now()
        ));
    }
}
