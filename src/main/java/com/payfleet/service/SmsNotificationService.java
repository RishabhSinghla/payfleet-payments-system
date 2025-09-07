package com.payfleet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SMS Notification Service - Handles SMS delivery
 * <p>
 * Enterprise Pattern: SMS Communication Service
 * Banking Context: Critical alerts via SMS for important payment events
 */
@Service
public class SmsNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);

    public boolean sendSms(String phoneNumber, String message) {
        // Simulate SMS sending (In production, use Twilio, AWS SNS, etc.)
        logger.info("SENDING SMS:");
        logger.info("To: {}", phoneNumber);
        logger.info("Message: {}", message);
        logger.info("--- SMS SENT SUCCESSFULLY ---");

        // Simulate occasional failures for testing
        if (Math.random() < 0.03) { // 3% failure rate
            logger.error("Simulated SMS delivery failure to: {}", phoneNumber);
            return false;
        }

        return true;
    }
}
