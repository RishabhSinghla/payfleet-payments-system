package com.payfleet.controller;

import com.payfleet.dto.AuthResponse;
import com.payfleet.dto.LoginRequest;
import com.payfleet.model.User;
import com.payfleet.security.JwtUtil;
import com.payfleet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller - Handles user authentication endpoints
 * <p>
 * Enterprise Pattern: Authentication API
 * - Provides login endpoint for JWT token generation
 * - Validates credentials and returns secure tokens
 * - Handles authentication errors gracefully
 * <p>
 * Banking Context: Secure authentication is critical for financial systems
 * Every API call must be authenticated and authorized
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * User Login Endpoint
     * <p>
     * POST /payfleet/api/v1/auth/login
     * <p>
     * Authenticates user credentials and returns JWT token
     *
     * @param loginRequest Username/email and password
     * @return JWT token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user credentials
            Optional<User> authenticatedUser = userService.authenticateUser(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            if (authenticatedUser.isPresent()) {
                User user = authenticatedUser.get();

                // Generate JWT token for authenticated user
                String token = jwtUtil.generateToken(user.getUsername());

                // Create response with token and user info
                AuthResponse authResponse = new AuthResponse(
                        token,
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole().name()
                );

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Login successful",
                        "data", authResponse,
                        "timestamp", LocalDateTime.now()
                ));

            } else {
                // Authentication failed
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", "Invalid username or password",
                        "timestamp", LocalDateTime.now()
                ));
            }

        } catch (IllegalStateException e) {
            // Account status issue (suspended, inactive, etc.)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            // Unexpected error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Login failed due to server error",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Token Validation Endpoint
     * <p>
     * GET /payfleet/api/v1/auth/validate
     * <p>
     * Validates JWT token (useful for frontend apps)
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "username", username,
                        "message", "Token is valid",
                        "timestamp", LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "valid", false,
                        "message", "Invalid or expired token",
                        "timestamp", LocalDateTime.now()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "valid", false,
                    "message", "Token validation failed",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}
