package com.payfleet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Email Notification Service - Handles email delivery
 * <p>
 * Enterprise Pattern: Email Communication Service
 * Banking Context: Professional email communications for payment events
 */
@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    public boolean sendEmail(String recipient, String subject, String message) {
        // Simulate email sending (In production, use Spring Mail or SendGrid)
        logger.info("SENDING EMAIL:");
        logger.info("To: {}", recipient);
        logger.info("Subject: {}", subject);
        logger.info("Message: {}", message);
        logger.info("--- EMAIL SENT SUCCESSFULLY ---");

        // Simulate occasional failures for testing
        if (Math.random() < 0.05) { // 5% failure rate
            logger.error("Simulated email delivery failure to: {}", recipient);
            return false;
        }

        return true;
    }
}
