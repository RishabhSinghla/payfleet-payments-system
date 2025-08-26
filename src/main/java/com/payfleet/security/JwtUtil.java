package com.payfleet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT Utility Class - Handles JWT token operations
 * <p>
 * Enterprise Pattern: Token-based Authentication
 * - Generates secure JWT tokens for authenticated users
 * - Validates tokens and extracts user information
 * - Handles token expiration and security
 * <p>
 * Banking Context: Stateless authentication for distributed systems
 * Used by all major financial institutions for API security
 */
@Component
public class JwtUtil {

    // Secret key for signing tokens (in production, use environment variable)
    private static final String SECRET_KEY = "PayFleetSecretKeyForJWTTokenGenerationAndValidation2025";

    // Token validity period (24 hours)
    private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    // Generate secret key from string
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Generate JWT token for authenticated user
     *
     * @param username The authenticated user's username
     * @return JWT token string
     */
    public String generateToken(String username) {
        return createToken(username);
    }

    /**
     * Create JWT token with claims and expiration
     *
     * @param subject The username (subject of the token)
     * @return JWT token string
     */
    private String createToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_TOKEN_VALIDITY);

        return Jwts.builder()
                .setSubject(subject)           // Username
                .setIssuedAt(now)              // Token creation time
                .setExpiration(expiryDate)     // Token expiration time
                .setIssuer("PayFleet")         // Token issuer
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Sign with secret key
                .compact();                    // Build the JWT string
    }

    /**
     * Extract username from JWT token
     *
     * @param token JWT token string
     * @return Username extracted from token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from JWT token
     *
     * @param token JWT token string
     * @return Expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token
     *
     * @param token          JWT token string
     * @param claimsResolver Function to extract specific claim
     * @return Extracted claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse JWT token and extract all claims
     *
     * @param token JWT token string
     * @return All claims from the token
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Check if JWT token has expired
     *
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Validate JWT token against username and expiration
     *
     * @param token    JWT token string
     * @param username Username to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate JWT token (general validation)
     *
     * @param token JWT token string
     * @return true if token is valid and not expired
     */
    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
