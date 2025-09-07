package com.payfleet.service;

import com.payfleet.dto.PaymentEvent;
import com.payfleet.dto.PaymentEventType;
import com.payfleet.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Payment Event Producer - Publishes payment events to Kafka topics
 * <p>
 * Enterprise Pattern: Event Publishing in Microservices
 * Banking Context: Publishes payment events for notifications and audit
 */
@Service
public class PaymentEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${payfleet.kafka.topics.payment-events}")
    private String paymentEventsTopic;

    @Autowired
    public PaymentEventProducer(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish Payment Initiated Event
     */
    public void publishPaymentInitiated(Payment payment) {
        PaymentEvent event = new PaymentEvent(
                PaymentEventType.PAYMENT_INITIATED,
                payment.getId(),
                payment.getPaymentReference(),
                payment.getFromAccount().getAccountNumber(),
                payment.getToAccount().getAccountNumber(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getDescription(),
                payment.getInitiatedBy().getUsername()
        );

        publishEvent(event, "Payment initiated: " + payment.getPaymentReference());
    }

    /**
     * Publish Payment Completed Event
     */
    public void publishPaymentCompleted(Payment payment) {
        PaymentEvent event = new PaymentEvent(
                PaymentEventType.PAYMENT_COMPLETED,
                payment.getId(),
                payment.getPaymentReference(),
                payment.getFromAccount().getAccountNumber(),
                payment.getToAccount().getAccountNumber(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getDescription(),
                payment.getInitiatedBy().getUsername()
        );

        event.setProcessingDetails(payment.getProcessingDetails());
        publishEvent(event, "Payment completed: " + payment.getPaymentReference());
    }

    /**
     * Publish Payment Failed Event
     */
    public void publishPaymentFailed(Payment payment) {
        PaymentEvent event = new PaymentEvent(
                PaymentEventType.PAYMENT_FAILED,
                payment.getId(),
                payment.getPaymentReference(),
                payment.getFromAccount().getAccountNumber(),
                payment.getToAccount().getAccountNumber(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getDescription(),
                payment.getInitiatedBy().getUsername()
        );

        event.setFailureReason(payment.getFailureReason());
        publishEvent(event, "Payment failed: " + payment.getPaymentReference());
    }

    /**
     * Generic method to publish events to Kafka
     */
    public void publishEvent(PaymentEvent event, String logMessage) {
        try {
            logger.info("Publishing event: {}", logMessage);

            CompletableFuture<SendResult<String, PaymentEvent>> future =
                    kafkaTemplate.send(paymentEventsTopic, event.getPaymentReference(), event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    logger.error("Failed to publish event: {} - Error: {}",
                            event.getEventType(), ex.getMessage());
                } else {
                    logger.info("Successfully published event: {} to partition {} at offset {}",
                            event.getEventType(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            logger.error("Error publishing payment event: {}", e.getMessage(), e);
        }
    }
}
