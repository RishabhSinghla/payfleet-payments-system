package com.payfleet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Notification Log Entity - Tracks all sent notifications
 * <p>
 * Enterprise Pattern: Notification Audit Trail
 * Banking Context: Complete record of all customer communications
 */
@Entity
@Table(name = "notification_logs",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_reference_id", columnList = "reference_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_channel", columnList = "channel"),
                @Index(name = "idx_created_at", columnList = "created_at")
        })
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User ID - Who received the notification
     */
    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    /**
     * Reference ID - Payment reference or other business entity ID
     */
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    /**
     * Notification Type - Type of notification sent
     */
    @Column(name = "notification_type", nullable = false, length = 100)
    private String notificationType;

    /**
     * Channel - Delivery channel used
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    /**
     * Recipient - Email address, phone number, or device token
     */
    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    /**
     * Subject - Notification subject (for email/push)
     */
    @Column(name = "subject", length = 500)
    private String subject;

    /**
     * Message - Notification message body
     */
    @Lob
    @Column(name = "message", nullable = false)
    private String message;

    /**
     * Status - Delivery status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    /**
     * Priority - Notification priority
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private NotificationPriority priority;

    /**
     * Sent At - When notification was sent
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Delivered At - When notification was delivered (if trackable)
     */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /**
     * Error Message - Error details if delivery failed
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * Retry Count - Number of retry attempts
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /**
     * Next Retry At - When to retry sending (if failed)
     */
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public NotificationLog() {
    }

    public NotificationLog(String userId, String referenceId, String notificationType,
                           NotificationChannel channel, String recipient, String subject,
                           String message, NotificationPriority priority) {
        this.userId = userId;
        this.referenceId = referenceId;
        this.notificationType = notificationType;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.priority = priority;
        this.status = NotificationStatus.PENDING;
    }

    // Getters and Setters (abbreviated for brevity)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
