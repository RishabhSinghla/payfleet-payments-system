package com.payfleet.service;

import com.payfleet.dto.PaymentEvent;
import com.payfleet.dto.PaymentEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Payment Event Consumer - Consumes payment events from Kafka topics
 * <p>
 * Enterprise Pattern: Event-Driven Processing
 * Banking Context: Handles payment notifications and downstream processing
 */
@Service
public class PaymentEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventConsumer.class);

    /**
     * Main Payment Event Listener
     */
    @KafkaListener(
            topics = "${payfleet.kafka.topics.payment-events}",
            groupId = "payfleet-payment-processor"
    )
    public void handlePaymentEvent(@Payload PaymentEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   Acknowledgment acknowledgment) {

        logger.info("Received payment event: {} from topic: {}, partition: {}, offset: {}",
                event.getEventType(), topic, partition, offset);

        try {
            // Route event to appropriate handler
            switch (event.getEventType()) {
                case PaymentEventType.PAYMENT_INITIATED:
                    handlePaymentInitiated(event);
                    break;

                case PaymentEventType.PAYMENT_COMPLETED:
                    handlePaymentCompleted(event);
                    break;

                case PaymentEventType.PAYMENT_FAILED:
                    handlePaymentFailed(event);
                    break;

                default:
                    logger.warn("Unknown event type: {}", event.getEventType());
            }

            // Acknowledge message processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("Error processing payment event: {} - Error: {}",
                    event.getEventType(), e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    private void handlePaymentInitiated(PaymentEvent event) {
        logger.info("Processing payment initiation: {}", event.getPaymentReference());
        // Here you could: send notifications, trigger fraud detection, log for audit
    }

    private void handlePaymentCompleted(PaymentEvent event) {
        logger.info("Processing payment completion: {}", event.getPaymentReference());
        // Here you could: send success notifications, update reporting, trigger accounting
    }

    private void handlePaymentFailed(PaymentEvent event) {
        logger.info("Processing payment failure: {} - Reason: {}",
                event.getPaymentReference(), event.getFailureReason());
        // Here you could: send failure notifications, trigger retry, alert support
    }
}
