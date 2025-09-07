package com.payfleet.service;

import com.payfleet.dto.PaymentEvent;
import com.payfleet.model.*;
import com.payfleet.repository.NotificationLogRepository;
import com.payfleet.repository.NotificationTemplateRepository;
import com.payfleet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Comprehensive Notification Service - Manages all notification operations
 * <p>
 * Enterprise Pattern: Multi-Channel Notification System
 * Banking Context: Real-time customer communication for all payment events
 */
@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;
    private final PushNotificationService pushService;

    @Autowired
    public NotificationService(NotificationTemplateRepository templateRepository,
                               NotificationLogRepository notificationLogRepository,
                               UserRepository userRepository,
                               EmailNotificationService emailService,
                               SmsNotificationService smsService,
                               PushNotificationService pushService) {
        this.templateRepository = templateRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.pushService = pushService;
    }

    /**
     * Process Payment Events for Notifications
     */
    @KafkaListener(topics = "${payfleet.kafka.topics.payment-events}", groupId = "notification-group")
    @Async
    public void processPaymentEventForNotifications(PaymentEvent event) {
        logger.info("Processing payment event for notifications: {} - {}",
                event.getEventType(), event.getPaymentReference());

        try {
            // Get user information
            Optional<User> userOptional = userRepository.findByUsername(event.getInitiatedByUsername());
            if (userOptional.isEmpty()) {
                logger.warn("User not found for notifications: {}", event.getInitiatedByUsername());
                return;
            }

            User user = userOptional.get();

            // Send notifications based on event type
            switch (event.getEventType()) {
                case "PAYMENT_INITIATED":
                    sendPaymentInitiatedNotifications(user, event);
                    break;

                case "PAYMENT_COMPLETED":
                    sendPaymentCompletedNotifications(user, event);
                    break;

                case "PAYMENT_FAILED":
                    sendPaymentFailedNotifications(user, event);
                    break;

                default:
                    logger.debug("No notifications configured for event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            logger.error("Failed to process payment event for notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * Send Payment Initiated Notifications
     */
    private void sendPaymentInitiatedNotifications(User user, PaymentEvent event) {
        String notificationType = "PAYMENT_INITIATED";

        // Prepare template variables
        Map<String, Object> variables = createTemplateVariables(user, event);

        // Send email notification
        sendNotificationByChannel(user, notificationType, NotificationChannel.EMAIL,
                event.getPaymentReference(), variables, NotificationPriority.MEDIUM);

        // Send push notification
        sendNotificationByChannel(user, notificationType, NotificationChannel.PUSH,
                event.getPaymentReference(), variables, NotificationPriority.MEDIUM);
    }

    /**
     * Send Payment Completed Notifications
     */
    private void sendPaymentCompletedNotifications(User user, PaymentEvent event) {
        String notificationType = "PAYMENT_COMPLETED";

        Map<String, Object> variables = createTemplateVariables(user, event);

        // Send email notification
        sendNotificationByChannel(user, notificationType, NotificationChannel.EMAIL,
                event.getPaymentReference(), variables, NotificationPriority.HIGH);

        // Send push notification
        sendNotificationByChannel(user, notificationType, NotificationChannel.PUSH,
                event.getPaymentReference(), variables, NotificationPriority.HIGH);

        // Send SMS for high-value transactions
        if (event.getAmount().compareTo(new java.math.BigDecimal("1000.00")) > 0) {
            sendNotificationByChannel(user, notificationType, NotificationChannel.SMS,
                    event.getPaymentReference(), variables, NotificationPriority.HIGH);
        }
    }

    /**
     * Send Payment Failed Notifications
     */
    private void sendPaymentFailedNotifications(User user, PaymentEvent event) {
        String notificationType = "PAYMENT_FAILED";

        Map<String, Object> variables = createTemplateVariables(user, event);

        // Always send email for failed payments
        sendNotificationByChannel(user, notificationType, NotificationChannel.EMAIL,
                event.getPaymentReference(), variables, NotificationPriority.HIGH);

        // Send push notification
        sendNotificationByChannel(user, notificationType, NotificationChannel.PUSH,
                event.getPaymentReference(), variables, NotificationPriority.HIGH);
    }

    /**
     * Send notification via specific channel
     */
    private void sendNotificationByChannel(User user, String notificationType,
                                           NotificationChannel channel, String referenceId,
                                           Map<String, Object> variables, NotificationPriority priority) {

        // Get template for notification type and channel
        Optional<NotificationTemplate> templateOptional = templateRepository
                .findByTemplateTypeAndChannelAndIsActive(notificationType, channel, true);

        if (templateOptional.isEmpty()) {
            logger.warn("No active template found for type: {} and channel: {}", notificationType, channel);
            return;
        }

        NotificationTemplate template = templateOptional.get();

        // Process template with variables
        String subject = processTemplate(template.getSubjectTemplate(), variables);
        String message = processTemplate(template.getBodyTemplate(), variables);

        // Determine recipient based on channel
        String recipient = getRecipientForChannel(user, channel);
        if (recipient == null) {
            logger.warn("No recipient found for user: {} and channel: {}", user.getUsername(), channel);
            return;
        }

        // Create notification log
        NotificationLog notificationLog = new NotificationLog(
                user.getUsername(), referenceId, notificationType, channel,
                recipient, subject, message, priority
        );

        // Send notification
        try {
            boolean sent = deliverNotification(channel, recipient, subject, message);

            if (sent) {
                notificationLog.setStatus(NotificationStatus.SENT);
                notificationLog.setSentAt(LocalDateTime.now());
                logger.info("Notification sent successfully: {} via {} to {}",
                        notificationType, channel, recipient);
            } else {
                notificationLog.setStatus(NotificationStatus.FAILED);
                notificationLog.setErrorMessage("Delivery failed");
                logger.error("Failed to send notification: {} via {} to {}",
                        notificationType, channel, recipient);
            }

        } catch (Exception e) {
            notificationLog.setStatus(NotificationStatus.FAILED);
            notificationLog.setErrorMessage(e.getMessage());
            logger.error("Exception sending notification: {}", e.getMessage(), e);
        }

        // Save notification log
        notificationLogRepository.save(notificationLog);
    }

    /**
     * Deliver notification via appropriate service
     */
    private boolean deliverNotification(NotificationChannel channel, String recipient,
                                        String subject, String message) {
        switch (channel) {
            case EMAIL:
                return emailService.sendEmail(recipient, subject, message);

            case SMS:
                return smsService.sendSms(recipient, message);

            case PUSH:
                return pushService.sendPushNotification(recipient, subject, message);

            default:
                logger.warn("Unsupported notification channel: {}", channel);
                return false;
        }
    }

    /**
     * Create template variables for notification processing
     */
    private Map<String, Object> createTemplateVariables(User user, PaymentEvent event) {
        Map<String, Object> variables = new HashMap<>();

        // User variables
        variables.put("firstName", user.getFirstName());
        variables.put("lastName", user.getLastName());
        variables.put("fullName", user.getFirstName() + " " + user.getLastName());
        variables.put("username", user.getUsername());

        // Payment variables
        variables.put("paymentReference", event.getPaymentReference());
        variables.put("amount", event.getAmount());
        variables.put("currency", event.getCurrency());
        variables.put("description", event.getDescription());
        variables.put("fromAccount", maskAccountNumber(event.getFromAccountNumber()));
        variables.put("toAccount", maskAccountNumber(event.getToAccountNumber()));
        variables.put("status", event.getStatus());
        variables.put("timestamp", event.getTimestamp());

        // Additional variables
        if (event.getFailureReason() != null) {
            variables.put("failureReason", event.getFailureReason());
        }
        if (event.getProcessingDetails() != null) {
            variables.put("processingDetails", event.getProcessingDetails());
        }

        return variables;
    }

    /**
     * Process template with variables (simple placeholder replacement)
     */
    private String processTemplate(String template, Map<String, Object> variables) {
        if (template == null) return "";

        String processed = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processed = processed.replace(placeholder, value);
        }

        return processed;
    }

    /**
     * Get recipient address/identifier for channel
     */
    private String getRecipientForChannel(User user, NotificationChannel channel) {
        switch (channel) {
            case EMAIL:
                return user.getEmail();

            case SMS:
                return user.getPhoneNumber(); // Assuming User has phoneNumber field

            case PUSH:
                return user.getUsername(); // Use username as device identifier

            default:
                return null;
        }
    }

    /**
     * Mask account number for security
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return "****";
        }
        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "****-****-" + lastFour;
    }

    /**
     * Get notification history for user
     */
    public List<NotificationLog> getNotificationHistory(String userId, int limit) {
        return notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId, limit);
    }

    /**
     * Get notification statistics
     */
    public Map<String, Object> getNotificationStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalNotifications", notificationLogRepository.count());
        stats.put("sentNotifications", notificationLogRepository.countByStatus(NotificationStatus.SENT));
        stats.put("failedNotifications", notificationLogRepository.countByStatus(NotificationStatus.FAILED));
        stats.put("pendingNotifications", notificationLogRepository.countByStatus(NotificationStatus.PENDING));

        // Get stats by channel
        for (NotificationChannel channel : NotificationChannel.values()) {
            stats.put(channel.name().toLowerCase() + "Notifications",
                    notificationLogRepository.countByChannel(channel));
        }

        return stats;
    }
}
