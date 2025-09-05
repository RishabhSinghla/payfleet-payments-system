package com.payfleet.service;

import com.payfleet.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Audit Service for Event Tracking
 * <p>
 * Enterprise Pattern: Event Sourcing and Audit Trail
 * Banking Context: Regulatory compliance and transaction tracking
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    public void logEventProcessed(PaymentEvent event) {
        logger.info("AUDIT: Event processed successfully - Type: {}, Payment: {}, Time: {}",
                event.getEventType(), event.getPaymentReference(), LocalDateTime.now());

        // In real implementation: store in audit database
        createAuditRecord("EVENT_PROCESSED", event, "Event processed successfully", null);
    }

    public void logEventProcessingFailed(PaymentEvent event, String errorMessage) {
        logger.error("AUDIT: Event processing failed - Type: {}, Payment: {}, Error: {}, Time: {}",
                event.getEventType(), event.getPaymentReference(), errorMessage, LocalDateTime.now());

        createAuditRecord("EVENT_PROCESSING_FAILED", event, "Event processing failed", errorMessage);
    }

    public void logPaymentInitiated(PaymentEvent event) {
        logger.info("AUDIT: Payment initiated - Reference: {}, Amount: {}, From: {}, To: {}",
                event.getPaymentReference(), event.getAmount(),
                event.getFromAccountNumber(), event.getToAccountNumber());

        createAuditRecord("PAYMENT_INITIATED", event, "Payment initiated", null);
    }

    public void logPaymentProcessing(PaymentEvent event) {
        logger.info("AUDIT: Payment processing started - Reference: {}", event.getPaymentReference());

        createAuditRecord("PAYMENT_PROCESSING", event, "Payment processing started", null);
    }

    public void logPaymentCompleted(PaymentEvent event) {
        logger.info("AUDIT: Payment completed - Reference: {}, Amount: {}",
                event.getPaymentReference(), event.getAmount());

        createAuditRecord("PAYMENT_COMPLETED", event, "Payment completed successfully", null);
    }

    public void logPaymentFailed(PaymentEvent event) {
        logger.error("AUDIT: Payment failed - Reference: {}, Reason: {}",
                event.getPaymentReference(), event.getFailureReason());

        createAuditRecord("PAYMENT_FAILED", event, "Payment failed", event.getFailureReason());
    }

    public void logAccountDebited(PaymentEvent event) {
        logger.info("AUDIT: Account debited - Account: {}, Amount: {}, Payment: {}",
                event.getFromAccountNumber(), event.getAmount(), event.getPaymentReference());

        createAuditRecord("ACCOUNT_DEBITED", event,
                "Account debited for payment", null);
    }

    public void logAccountCredited(PaymentEvent event) {
        logger.info("AUDIT: Account credited - Account: {}, Amount: {}, Payment: {}",
                event.getToAccountNumber(), event.getAmount(), event.getPaymentReference());

        createAuditRecord("ACCOUNT_CREDITED", event,
                "Account credited from payment", null);
    }

    private void createAuditRecord(String auditType, PaymentEvent event,
                                   String description, String errorDetails) {
        // Simulate audit record creation
        logger.debug("Creating audit record: Type={}, PaymentRef={}, Description={}",
                auditType, event.getPaymentReference(), description);

        // In real implementation:
        // - Store in dedicated audit database (append-only)
        // - Include all event details, timestamps, user context
        // - Implement data retention policies
        // - Ensure immutability for regulatory compliance
        // - Index for efficient querying and reporting
    }
}
