package com.payfleet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payfleet.dto.PaymentInitiationRequest;
import com.payfleet.dto.PaymentResponse;
import com.payfleet.service.IdempotencyService;
import com.payfleet.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Payment Controller - REST API for payment processing
 * <p>
 * Enterprise Payment APIs:
 * - Payment initiation with comprehensive validation
 * - Payment status tracking and history
 * - Transaction management with proper error handling
 * - Security through JWT authentication and authorization
 * <p>
 * Banking Context: Core APIs for payment processing operations
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public PaymentController(PaymentService paymentService, IdempotencyService idempotencyService) {
        this.paymentService = paymentService;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Initiate Payment
     * <p>
     * POST /api/v1/payments/initiate
     * <p>
     * Initiates a new payment transaction between accounts
     */
    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody PaymentInitiationRequest request,
                                             @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                             Authentication authentication) {

        String username = authentication.getName();

        try {
            // Check idempotency if key provided
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                IdempotencyService.IdempotencyResult idempotencyResult =
                        idempotencyService.checkIdempotency(idempotencyKey, "Payment", username, request);

                // Handle different idempotency results
                switch (idempotencyResult.getAction()) {
                    case RETURN_DUPLICATE:
                        return ResponseEntity.status(idempotencyResult.getHttpStatus())
                                .body(idempotencyResult.getResponseBody());

                    case PROCESSING:
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                                "success", false,
                                "message", "Request is still being processed",
                                "idempotencyKey", idempotencyKey,
                                "timestamp", LocalDateTime.now()
                        ));

                    case CONFLICT:
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                                "success", false,
                                "message", idempotencyResult.getMessage(),
                                "idempotencyKey", idempotencyKey,
                                "timestamp", LocalDateTime.now()
                        ));

                    case PROCEED:
                        // Continue with normal processing
                        break;
                }
            }

            // Process payment normally
            PaymentResponse payment = paymentService.initiatePayment(request, username);

            // Create success response
            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "Payment initiated successfully",
                    "data", payment,
                    "timestamp", LocalDateTime.now()
            );

            // Mark idempotency as completed if key was provided
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                try {
                    String responseBody = objectMapper.writeValueAsString(response);
                    idempotencyService.markCompleted(idempotencyKey, payment.getPaymentReference(),
                            responseBody, HttpStatus.CREATED.value());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // Handle business logic errors
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            );

            // Mark idempotency as failed if key was provided
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                try {
                    String errorBody = objectMapper.writeValueAsString(errorResponse);
                    idempotencyService.markFailed(idempotencyKey, errorBody, HttpStatus.BAD_REQUEST.value());
                } catch (Exception ex) {
                    throw new RuntimeException(e);
                }
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            // Handle unexpected errors
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "Payment initiation failed due to server error",
                    "timestamp", LocalDateTime.now()
            );

            // Mark idempotency as failed if key was provided
            if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
                try {
                    String errorBody = objectMapper.writeValueAsString(errorResponse);
                    idempotencyService.markFailed(idempotencyKey, errorBody, HttpStatus.INTERNAL_SERVER_ERROR.value());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get Payment by Reference
     * <p>
     * GET /api/v1/payments/{paymentReference}
     * <p>
     * Retrieves payment details by payment reference
     */
    @GetMapping("/{paymentReference}")
    public ResponseEntity<?> getPaymentByReference(@PathVariable String paymentReference) {
        try {
            Optional<PaymentResponse> paymentOptional =
                    paymentService.getPaymentByReference(paymentReference);

            if (paymentOptional.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Payment details retrieved successfully",
                        "data", paymentOptional.get(),
                        "timestamp", LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Payment not found",
                        "timestamp", LocalDateTime.now()
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve payment details",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get My Payment History
     * <p>
     * GET /api/v1/payments/my-payments
     * <p>
     * Retrieves all payments initiated by the authenticated user
     */
    @GetMapping("/my-payments")
    public ResponseEntity<?> getMyPayments(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<PaymentResponse> payments = paymentService.getUserPayments(username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment history retrieved successfully",
                    "data", payments,
                    "count", payments.size(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve payment history",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Get Account Payment History
     * <p>
     * GET /api/v1/payments/account/{accountNumber}/history
     * <p>
     * Retrieves payment history for a specific account
     */
    @GetMapping("/account/{accountNumber}/history")
    public ResponseEntity<?> getAccountPaymentHistory(@PathVariable String accountNumber) {
        try {
            List<PaymentResponse> payments = paymentService.getAccountPaymentHistory(accountNumber);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Account payment history retrieved successfully",
                    "data", payments,
                    "count", payments.size(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to retrieve account payment history",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * Cancel Payment
     * <p>
     * PUT /api/v1/payments/{paymentId}/cancel
     * <p>
     * Cancels a pending payment
     */
    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long paymentId,
                                           Authentication authentication) {
        try {
            String username = authentication.getName();
            PaymentResponse payment = paymentService.cancelPayment(paymentId, username);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment cancelled successfully",
                    "data", payment,
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to cancel payment",
                    "timestamp", LocalDateTime.now()
            ));
        }
    }
}
