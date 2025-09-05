package com.payfleet.service;

import com.payfleet.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Notification Service for Payment Events
 * <p>
 * Enterprise Pattern: Event-Driven Notifications
 * Banking Context: Real-time customer and system notifications
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void sendPaymentInitiatedNotification(PaymentEvent event) {
        logger.info("Sending payment initiated notification for: {} to user: {}",
                event.getPaymentReference(), event.getInitiatedByUsername());

        // Simulate email/SMS notification
        String message = String.format(
                "Payment %s for $%s has been initiated. We'll notify you when it's processed.",
                event.getPaymentReference(), event.getAmount()
        );

        sendNotification(event.getInitiatedByUsername(), "Payment Initiated", message);
    }

    public void sendPaymentCompletedNotification(PaymentEvent event) {
        logger.info("Sending payment completed notification for: {} to user: {}",
                event.getPaymentReference(), event.getInitiatedByUsername());

        String message = String.format(
                "Payment %s for $%s has been completed successfully. Funds have been transferred.",
                event.getPaymentReference(), event.getAmount()
        );

        sendNotification(event.getInitiatedByUsername(), "Payment Completed", message);
    }

    public void sendPaymentFailedNotification(PaymentEvent event) {
        logger.info("Sending payment failed notification for: {} to user: {}",
                event.getPaymentReference(), event.getInitiatedByUsername());

        String message = String.format(
                "Payment %s for $%s has failed. Reason: %s. Please try again or contact support.",
                event.getPaymentReference(), event.getAmount(), event.getFailureReason()
        );

        sendNotification(event.getInitiatedByUsername(), "Payment Failed", message);
    }

    public void sendAccountCreditedNotification(PaymentEvent event) {
        logger.info("Sending account credited notification for account: {}",
                event.getToAccountNumber());

        String message = String.format(
                "Your account %s has been credited with $%s from payment %s.",
                maskAccountNumber(event.getToAccountNumber()),
                event.getAmount(),
                event.getPaymentReference()
        );

        // In real implementation: find account owner and send notification
        sendNotification("account-owner", "Account Credited", message);
    }

    public void alertSupportTeam(PaymentEvent event) {
        logger.warn("Alerting support team for failed payment: {} - Reason: {}",
                event.getPaymentReference(), event.getFailureReason());

        String alertMessage = String.format(
                "ALERT: Payment %s failed with reason: %s. Manual review required.",
                event.getPaymentReference(), event.getFailureReason()
        );

        sendNotification("support-team", "Payment Failure Alert", alertMessage);
    }

    private void sendNotification(String recipient, String subject, String message) {
        // Simulate notification sending
        logger.info("NOTIFICATION -> To: {} | Subject: {} | Message: {}",
                recipient, subject, message);

        // In real implementation:
        // - Send email via email service (SendGrid, SES)
        // - Send SMS via SMS service (Twilio, SNS)
        // - Send push notification via mobile service
        // - Store notification in database for tracking
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return "****";
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****-****-" + lastFour;
    }
}
