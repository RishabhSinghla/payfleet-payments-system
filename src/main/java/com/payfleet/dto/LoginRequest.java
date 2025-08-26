package com.payfleet.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login Request DTO - Captures user login credentials
 * <p>
 * Used for POST /api/v1/auth/login endpoint
 * Validates that both username and password are provided
 */
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Default constructor
    public LoginRequest() {
    }

    // Constructor for testing
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{username='" + username + "'}"; // Never include password in toString!
    }
}
