package com.payfleet.controller;

import com.payfleet.dto.UserProfileUpdateRequest;
import com.payfleet.dto.UserRegistrationRequest;
import com.payfleet.dto.UserResponse;
import com.payfleet.model.User;
import com.payfleet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Get Current User Profile - Protected Endpoint
     * <p>
     * GET /api/v1/users/profile
     * <p>
     * Returns current authenticated user's profile information
     * Requires valid JWT token in Authorization header
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isPresent()) {
                UserResponse userResponse = new UserResponse(userOptional.get());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Profile retrieved successfully",
                        "data", userResponse,
                        "timestamp", LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "User not found",
                        "timestamp", LocalDateTime.now()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve profile",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Update User Profile - Protected Endpoint
     * <p>
     * PUT /api/v1/users/profile
     * <p>
     * Updates current authenticated user's profile information
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody @Valid UserProfileUpdateRequest request,
                                               Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Update allowed fields
                user.setFirstName(request.getFirstName());
                user.setLastName(request.getLastName());
                user.setEmail(request.getEmail());

                // Save updated user
                User updatedUser = userService.updateUser(user);
                UserResponse userResponse = new UserResponse(updatedUser);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Profile updated successfully",
                        "data", userResponse,
                        "timestamp", LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "User not found",
                        "timestamp", LocalDateTime.now()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to update profile",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Admin Only - Get All Users
     * <p>
     * GET /api/v1/users/all
     * <p>
     * Returns all users (admin only endpoint)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        try {
            List<User> users = userService.findAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Users retrieved successfully",
                    "data", userResponses,
                    "count", userResponses.size(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve users",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

}
