package com.payfleet.service;

import com.payfleet.dto.PaymentEvent;
import com.payfleet.dto.PaymentEventType;
import com.payfleet.model.Payment;
import com.payfleet.model.PaymentStatus;
import com.payfleet.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Advanced Payment Event Processor
 * <p>
 * Enterprise Pattern: Event-Driven State Management
 * Banking Context: Asynchronous payment processing with state transitions
 */
@Service
public class PaymentEventProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventProcessor.class);

    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    @Autowired
    private EventStoreService eventStoreService;

    @Autowired
    public PaymentEventProcessor(PaymentRepository paymentRepository,
                                 NotificationService notificationService,
                                 AuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Process Payment Events with Retry Logic
     */
    @KafkaListener(topics = "${payfleet.kafka.topics.payment-events}")
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR
    )
    @Transactional
    public void processPaymentEvent(@Payload PaymentEvent event,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset,
                                    Acknowledgment acknowledgment) {

        logger.info("Processing payment event: {} for payment: {} from partition: {}, offset: {}",
                event.getEventType(), event.getPaymentReference(), partition, offset);

        try {
            // Store event in event store for audit trail
            eventStoreService.storePaymentEvent(event, event.getInitiatedByUsername(),
                    event.getPaymentReference());
            // Process based on event type
            switch (event.getEventType()) {
                case PaymentEventType.PAYMENT_INITIATED:
                    handlePaymentInitiated(event);
                    break;

                case PaymentEventType.PAYMENT_PROCESSING:
                    handlePaymentProcessing(event);
                    break;

                case PaymentEventType.PAYMENT_COMPLETED:
                    handlePaymentCompleted(event);
                    break;

                case PaymentEventType.PAYMENT_FAILED:
                    handlePaymentFailed(event);
                    break;

                case PaymentEventType.ACCOUNT_DEBITED:
                    handleAccountDebited(event);
                    break;

                case PaymentEventType.ACCOUNT_CREDITED:
                    handleAccountCredited(event);
                    break;

                default:
                    logger.warn("Unknown event type: {} for payment: {}",
                            event.getEventType(), event.getPaymentReference());
            }

            // Log successful processing
            auditService.logEventProcessed(event);
            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("Failed to process payment event: {} for payment: {} - Error: {}",
                    event.getEventType(), event.getPaymentReference(), e.getMessage(), e);

            // Log the failure for monitoring
            auditService.logEventProcessingFailed(event, e.getMessage());

            // Re-throw to trigger retry mechanism
            throw new RuntimeException("Payment event processing failed", e);
        }
    }

    /**
     * Handle Payment Initiated Events
     */
    private void handlePaymentInitiated(PaymentEvent event) {
        logger.info("Processing payment initiation for: {}", event.getPaymentReference());

        // Update payment status in database
        updatePaymentStatus(event.getPaymentReference(), PaymentStatus.PENDING);

        // Send initiation notification
        notificationService.sendPaymentInitiatedNotification(event);

        // Trigger fraud detection (simulate)
        performFraudDetection(event);

        // Log audit trail
        auditService.logPaymentInitiated(event);
    }

    /**
     * Handle Payment Processing Events
     */
    private void handlePaymentProcessing(PaymentEvent event) {
        logger.info("Processing payment processing for: {}", event.getPaymentReference());

        // Update payment status
        updatePaymentStatus(event.getPaymentReference(), PaymentStatus.PROCESSING);

        // Log processing start
        auditService.logPaymentProcessing(event);
    }

    /**
     * Handle Payment Completed Events
     */
    private void handlePaymentCompleted(PaymentEvent event) {
        logger.info("Processing payment completion for: {}", event.getPaymentReference());

        // Update payment status
        updatePaymentStatus(event.getPaymentReference(), PaymentStatus.COMPLETED);

        // Send success notification
        notificationService.sendPaymentCompletedNotification(event);

        // Update customer loyalty points (simulate)
        updateLoyaltyPoints(event);

        // Trigger accounting processes (simulate)
        triggerAccountingProcess(event);

        // Log completion
        auditService.logPaymentCompleted(event);
    }

    /**
     * Handle Payment Failed Events
     */
    private void handlePaymentFailed(PaymentEvent event) {
        logger.info("Processing payment failure for: {} - Reason: {}",
                event.getPaymentReference(), event.getFailureReason());

        // Update payment status
        updatePaymentStatus(event.getPaymentReference(), PaymentStatus.FAILED);

        // Send failure notification
        notificationService.sendPaymentFailedNotification(event);

        // Check if retry is possible
        checkRetryEligibility(event);

        // Alert support team for manual review
        notificationService.alertSupportTeam(event);

        // Log failure
        auditService.logPaymentFailed(event);
    }

    /**
     * Handle Account Debited Events
     */
    private void handleAccountDebited(PaymentEvent event) {
        logger.info("Processing account debit for account: {} amount: {}",
                event.getFromAccountNumber(), event.getAmount());

        // Update account balance in read model (if using CQRS)
        updateAccountBalanceReadModel(event.getFromAccountNumber(), event.getAmount().negate());

        // Check for low balance alerts
        checkLowBalanceAlert(event.getFromAccountNumber());

        // Update spending analytics
        updateSpendingAnalytics(event);

        // Log debit operation
        auditService.logAccountDebited(event);
    }

    /**
     * Handle Account Credited Events
     */
    private void handleAccountCredited(PaymentEvent event) {
        logger.info("Processing account credit for account: {} amount: {}",
                event.getToAccountNumber(), event.getAmount());

        // Update account balance in read model
        updateAccountBalanceReadModel(event.getToAccountNumber(), event.getAmount());

        // Send credit notification
        notificationService.sendAccountCreditedNotification(event);

        // Update earning analytics
        updateEarningAnalytics(event);

        // Log credit operation
        auditService.logAccountCredited(event);
    }

    /**
     * Helper Methods for Business Logic
     */

    private void updatePaymentStatus(String paymentReference, PaymentStatus newStatus) {
        Optional<Payment> paymentOptional = paymentRepository.findByPaymentReference(paymentReference);
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            payment.setStatus(newStatus);
            paymentRepository.save(payment);
            logger.debug("Updated payment {} status to {}", paymentReference, newStatus);
        } else {
            logger.warn("Payment not found for reference: {}", paymentReference);
        }
    }

    private void performFraudDetection(PaymentEvent event) {
        // Simulate fraud detection logic
        logger.info("Performing fraud detection for payment: {}", event.getPaymentReference());
        // In real implementation: call fraud detection service, check patterns, etc.
    }

    private void updateLoyaltyPoints(PaymentEvent event) {
        // Simulate loyalty points update
        logger.info("Updating loyalty points for payment: {}", event.getPaymentReference());
        // In real implementation: calculate points, update customer profile
    }

    private void triggerAccountingProcess(PaymentEvent event) {
        // Simulate accounting process trigger
        logger.info("Triggering accounting process for payment: {}", event.getPaymentReference());
        // In real implementation: update general ledger, create journal entries
    }

    private void checkRetryEligibility(PaymentEvent event) {
        // Simulate retry eligibility check
        logger.info("Checking retry eligibility for failed payment: {}", event.getPaymentReference());
        // In real implementation: check failure reason, retry count, business rules
    }

    private void updateAccountBalanceReadModel(String accountNumber, java.math.BigDecimal amount) {
        // Simulate read model update for CQRS pattern
        logger.debug("Updating account balance read model for account: {} amount: {}", accountNumber, amount);
        // In real implementation: update denormalized read model tables
    }

    private void checkLowBalanceAlert(String accountNumber) {
        // Simulate low balance check
        logger.debug("Checking low balance alert for account: {}", accountNumber);
        // In real implementation: check balance threshold, send alerts
    }

    private void updateSpendingAnalytics(PaymentEvent event) {
        // Simulate spending analytics update
        logger.debug("Updating spending analytics for payment: {}", event.getPaymentReference());
        // In real implementation: update analytics tables, ML models
    }

    private void updateEarningAnalytics(PaymentEvent event) {
        // Simulate earning analytics update
        logger.debug("Updating earning analytics for payment: {}", event.getPaymentReference());
        // In real implementation: update analytics tables, reporting data
    }
}
