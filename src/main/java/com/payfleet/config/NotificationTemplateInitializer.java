package com.payfleet.config;

import com.payfleet.model.NotificationChannel;
import com.payfleet.model.NotificationPriority;
import com.payfleet.model.NotificationTemplate;
import com.payfleet.repository.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialize default notification templates
 */
@Component
public class NotificationTemplateInitializer implements CommandLineRunner {

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeTemplates();
    }

    private void initializeTemplates() {
        // Payment Initiated - Email
        createTemplateIfNotExists("PAYMENT_INITIATED", NotificationChannel.EMAIL,
                "Payment Initiated - ${paymentReference}",
                "Dear ${firstName},\n\n" +
                        "Your payment of ${currency} ${amount} has been initiated.\n\n" +
                        "Payment Reference: ${paymentReference}\n" +
                        "Description: ${description}\n" +
                        "From Account: ${fromAccount}\n" +
                        "To Account: ${toAccount}\n\n" +
                        "We'll notify you once the payment is processed.\n\n" +
                        "Best regards,\n" +
                        "PayFleet Team");

        // Payment Initiated - Push
        createTemplateIfNotExists("PAYMENT_INITIATED", NotificationChannel.PUSH,
                "Payment Initiated",
                "Payment of ${currency} ${amount} initiated successfully. Ref: ${paymentReference}");

        // Payment Completed - Email
        createTemplateIfNotExists("PAYMENT_COMPLETED", NotificationChannel.EMAIL,
                "Payment Completed - ${paymentReference}",
                "Dear ${firstName},\n\n" +
                        "Great news! Your payment has been completed successfully.\n\n" +
                        "Payment Details:\n" +
                        "Reference: ${paymentReference}\n" +
                        "Amount: ${currency} ${amount}\n" +
                        "Description: ${description}\n" +
                        "From: ${fromAccount}\n" +
                        "To: ${toAccount}\n" +
                        "Completed: ${timestamp}\n\n" +
                        "Thank you for using PayFleet!\n\n" +
                        "Best regards,\n" +
                        "PayFleet Team");

        // Payment Completed - Push
        createTemplateIfNotExists("PAYMENT_COMPLETED", NotificationChannel.PUSH,
                "Payment Completed",
                "✅ Payment of ${currency} ${amount} completed successfully!");

        // Payment Completed - SMS (for high-value transactions)
        createTemplateIfNotExists("PAYMENT_COMPLETED", NotificationChannel.SMS,
                null,
                "PayFleet: Payment ${paymentReference} of ${currency} ${amount} completed successfully. Thank you!");

        // Payment Failed - Email
        createTemplateIfNotExists("PAYMENT_FAILED", NotificationChannel.EMAIL,
                "Payment Failed - ${paymentReference}",
                "Dear ${firstName},\n\n" +
                        "We're sorry to inform you that your payment could not be processed.\n\n" +
                        "Payment Details:\n" +
                        "Reference: ${paymentReference}\n" +
                        "Amount: ${currency} ${amount}\n" +
                        "Description: ${description}\n" +
                        "Reason: ${failureReason}\n\n" +
                        "Please try again or contact our support team if the issue persists.\n\n" +
                        "Best regards,\n" +
                        "PayFleet Team");

        // Payment Failed - Push
        createTemplateIfNotExists("PAYMENT_FAILED", NotificationChannel.PUSH,
                "Payment Failed",
                "❌ Payment of ${currency} ${amount} failed. Please try again or contact support.");
    }

    private void createTemplateIfNotExists(String templateType, NotificationChannel channel,
                                           String subject, String body) {
        var existing = templateRepository.findByTemplateTypeAndChannelAndIsActive(
                templateType, channel, true);

        if (existing.isEmpty()) {
            NotificationTemplate template = new NotificationTemplate(
                    templateType, channel,
                    templateType + " - " + channel.name(),
                    subject, body);
            template.setPriority(NotificationPriority.MEDIUM);
            templateRepository.save(template);
        }
    }
}
