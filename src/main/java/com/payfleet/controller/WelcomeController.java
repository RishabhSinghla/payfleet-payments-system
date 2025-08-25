package com.payfleet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Welcome Controller - Our first REST API endpoint
 * <p>
 * This demonstrates enterprise Spring Boot REST API patterns:
 * - Clean controller structure
 * - Proper HTTP methods
 * - JSON response formatting
 * - API documentation
 *
 * @RestController combines @Controller + @ResponseBody
 * Automatically converts return objects to JSON
 */
@RestController
@RequestMapping("/api/v1")  // Base path for all APIs: /payfleet/api/v1
public class WelcomeController {

    /**
     * System Health and Welcome Endpoint
     * <p>
     * GET /payfleet/api/v1/welcome
     * <p>
     * Returns system status and basic information.
     * This pattern is used in enterprise systems for health checks.
     */
    @GetMapping("/welcome")
    public Map<String, Object> welcome() {
        return Map.of(
                "message", "🚀 Welcome to PayFleet Payment Processing System!",
                "status", "OPERATIONAL",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0-SNAPSHOT",
                "description", "Enterprise-grade payment system built for JPMC excellence",
                "features", new String[]{
                        "JWT Authentication",
                        "Real-time Payment Processing",
                        "Event-driven Architecture",
                        "Microservices Patterns",
                        "Production-grade Security"
                }
        );
    }

    /**
     * System Status Endpoint
     * <p>
     * GET /payfleet/api/v1/status
     * <p>
     * Quick health check endpoint - used by load balancers
     * and monitoring systems to verify application health.
     */
    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of(
                "status", "UP",
                "database", "CONNECTED",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
