package com.payfleet.controller;

import com.payfleet.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Idempotency Controller - Admin APIs for idempotency management
 * <p>
 * Enterprise Pattern: Idempotency Administration
 * Banking Context: Admin tools for duplicate prevention monitoring
 */
@RestController
@RequestMapping("/api/v1/admin/idempotency")
@PreAuthorize("hasRole('ADMIN')")
public class IdempotencyController {

    private final IdempotencyService idempotencyService;

    @Autowired
    public IdempotencyController(IdempotencyService idempotencyService) {
        this.idempotencyService = idempotencyService;
    }

    /**
     * Get idempotency statistics
     * <p>
     * GET /api/v1/admin/idempotency/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getIdempotencyStatistics() {
        try {
            IdempotencyService.IdempotencyStatistics statistics = idempotencyService.getStatistics();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Idempotency statistics retrieved successfully",
                    "data", statistics,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve statistics: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Clean up expired idempotency records
     * <p>
     * POST /api/v1/admin/idempotency/cleanup
     */
    @PostMapping("/cleanup")
    public ResponseEntity<?> cleanupExpiredRecords() {
        try {
            int cleanedUp = idempotencyService.cleanupExpiredRecords();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Expired records cleaned up successfully",
                    "cleanedUpCount", cleanedUp,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to cleanup records: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}
