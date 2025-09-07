package com.payfleet.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Notification Template Entity - Templates for different notification types
 * <p>
 * Enterprise Pattern: Template-Based Notification System
 * Banking Context: Consistent messaging across all communication channels
 */
@Entity
@Table(name = "notification_templates",
        indexes = {
                @Index(name = "idx_template_type", columnList = "template_type"),
                @Index(name = "idx_channel", columnList = "channel"),
                @Index(name = "idx_active", columnList = "is_active")
        })
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Template Type - Type of notification (PAYMENT_INITIATED, PAYMENT_COMPLETED, etc.)
     */
    @Column(name = "template_type", nullable = false, length = 100)
    private String templateType;

    /**
     * Channel - Delivery channel (EMAIL, SMS, PUSH)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    /**
     * Template Name - Human-readable template name
     */
    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    /**
     * Subject Template - Subject line for email/push notifications
     */
    @Column(name = "subject_template", length = 500)
    private String subjectTemplate;

    /**
     * Body Template - Message body with placeholder variables
     */
    @Lob
    @Column(name = "body_template", nullable = false)
    private String bodyTemplate;

    /**
     * Is Active - Whether this template is currently active
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Priority - Template priority (HIGH, MEDIUM, LOW)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public NotificationTemplate() {
    }

    public NotificationTemplate(String templateType, NotificationChannel channel,
                                String templateName, String subjectTemplate, String bodyTemplate) {
        this.templateType = templateType;
        this.channel = channel;
        this.templateName = templateName;
        this.subjectTemplate = subjectTemplate;
        this.bodyTemplate = bodyTemplate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
