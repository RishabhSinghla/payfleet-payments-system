package com.payfleet.dto;

import java.time.LocalDateTime;

/**
 * Authentication Response DTO - Returns JWT token and user info
 * <p>
 * Returned by login endpoint upon successful authentication
 * Contains JWT token for subsequent API calls
 */
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String fullName;
    private String role;
    private LocalDateTime expiresAt;
    private LocalDateTime issuedAt;

    // Default constructor
    public AuthResponse() {
    }

    // Constructor with essential fields
    public AuthResponse(String token, String username, String fullName, String role) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(24); // 24-hour expiration
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
