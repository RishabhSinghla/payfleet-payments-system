package com.payfleet.controller;

import com.payfleet.model.EventStore;
import com.payfleet.service.EventStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Audit Trail Controller - APIs for regulatory compliance and audit reporting
 * <p>
 * Enterprise Pattern: Audit Trail Management
 * Banking Context: Regulatory reporting and compliance APIs
 */
@RestController
@RequestMapping("/api/v1/audit")
public class AuditTrailController {

    private final EventStoreService eventStoreService;

    @Autowired
    public AuditTrailController(EventStoreService eventStoreService) {
        this.eventStoreService = eventStoreService;
    }

    /**
     * Get Payment Event History
     * <p>
     * GET /api/v1/audit/payments/{paymentReference}/history
     */
    @GetMapping("/payments/{paymentReference}/history")
    public ResponseEntity<?> getPaymentHistory(@PathVariable String paymentReference,
                                               Authentication authentication) {
        try {
            List<EventStore> events = eventStoreService.getPaymentEventHistory(paymentReference);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment history retrieved successfully",
                    "data", events,
                    "count", events.size(),
                    "paymentReference", paymentReference,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve payment history: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Payment Timeline for Detailed Audit
     * <p>
     * GET /api/v1/audit/payments/{paymentReference}/timeline
     */
    @GetMapping("/payments/{paymentReference}/timeline")
    public ResponseEntity<?> getPaymentTimeline(@PathVariable String paymentReference,
                                                Authentication authentication) {
        try {
            List<EventStoreService.PaymentTimelineEntry> timeline =
                    eventStoreService.getPaymentTimeline(paymentReference);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment timeline retrieved successfully",
                    "data", timeline,
                    "count", timeline.size(),
                    "paymentReference", paymentReference,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve payment timeline: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Replay Payment Events (Reconstruct State)
     * <p>
     * GET /api/v1/audit/payments/{paymentReference}/replay
     */
    @GetMapping("/payments/{paymentReference}/replay")
    public ResponseEntity<?> replayPaymentEvents(@PathVariable String paymentReference,
                                                 Authentication authentication) {
        try {
            var currentState = eventStoreService.replayPaymentEvents(paymentReference);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment state reconstructed successfully",
                    "data", currentState,
                    "paymentReference", paymentReference,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to replay payment events: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Audit Trail by Criteria (Admin Only)
     * <p>
     * GET /api/v1/audit/trail
     */
    @GetMapping("/trail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAuditTrail(
            @RequestParam(required = false) String aggregateType,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {

        try {
            List<EventStore> auditTrail = eventStoreService.getAuditTrail(
                    aggregateType, eventType, userId, startDate, endDate);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Audit trail retrieved successfully",
                    "data", auditTrail,
                    "count", auditTrail.size(),
                    "filters", Map.of(
                            "aggregateType", aggregateType,
                            "eventType", eventType,
                            "userId", userId,
                            "startDate", startDate,
                            "endDate", endDate
                    ),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve audit trail: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Events by User (Admin Only)
     * <p>
     * GET /api/v1/audit/users/{userId}/events
     */
    @GetMapping("/users/{userId}/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserEvents(@PathVariable String userId,
                                           Authentication authentication) {
        try {
            List<EventStore> events = eventStoreService.getEventsByUser(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User events retrieved successfully",
                    "data", events,
                    "count", events.size(),
                    "userId", userId,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve user events: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Event Statistics (Admin Only)
     * <p>
     * GET /api/v1/audit/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEventStatistics(Authentication authentication) {
        try {
            EventStoreService.EventStatistics statistics = eventStoreService.getEventStatistics();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Event statistics retrieved successfully",
                    "data", statistics,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve event statistics: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Events by Date Range (Admin Only)
     * <p>
     * GET /api/v1/audit/events/date-range
     */
    @GetMapping("/events/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {

        try {
            List<EventStore> events = eventStoreService.getEventsByDateRange(startDate, endDate);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Events retrieved successfully",
                    "data", events,
                    "count", events.size(),
                    "dateRange", Map.of(
                            "startDate", startDate,
                            "endDate", endDate
                    ),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve events: " + e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}
