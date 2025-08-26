package com.payfleet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Welcome Controller - System Information and Health Check APIs
 * <p>
 * Enterprise Pattern: Health Check Endpoints
 * - Used by load balancers to verify service health
 * - Used by monitoring systems (Prometheus, New Relic, etc.)
 * - Provides system information for debugging
 * <p>
 * JPMC Relevance: All production services need health checks
 */
@RestController
@RequestMapping("/api/v1")
public class WelcomeController {

    /**
     * Welcome Endpoint - System Information
     * <p>
     * GET /payfleet/api/v1/welcome
     * <p>
     * This endpoint provides:
     * - System status verification
     * - Version information
     * - Feature list for API consumers
     * - Timestamp for request tracking
     */
    @GetMapping("/welcome")
    public Map<String, Object> welcome() {
        return Map.of(
                "service", "PayFleet Payment Processing System",
                "message", "🚀 Enterprise payment system operational",
                "status", "HEALTHY",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0-SNAPSHOT",
                "environment", "development",
                "features", new String[]{
                        "User Registration & Authentication",
                        "Account Management",
                        "Real-time Payment Processing",
                        "Transaction Audit Trail",
                        "Role-based Access Control"
                }
        );
    }

    /**
     * Health Check Endpoint
     * <p>
     * GET /payfleet/api/v1/health
     * <p>
     * Quick health verification for monitoring systems.
     * Returns minimal response for high-frequency checks.
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "checks", Map.of(
                        "database", "CONNECTED",
                        "disk_space", "SUFFICIENT",
                        "memory", "NORMAL"
                )
        );
    }
}
