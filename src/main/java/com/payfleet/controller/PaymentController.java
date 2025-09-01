package com.payfleet.controller;

import com.payfleet.dto.PaymentInitiationRequest;
import com.payfleet.dto.PaymentResponse;
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

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
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
                                             Authentication authentication) {
        try {
            String username = authentication.getName();
            PaymentResponse payment = paymentService.initiatePayment(request, username);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Payment initiated successfully",
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
                    "message", "Payment initiation failed due to server error",
                    "timestamp", LocalDateTime.now()
            ));
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
