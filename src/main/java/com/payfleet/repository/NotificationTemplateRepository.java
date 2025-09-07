package com.payfleet.repository;

import com.payfleet.model.NotificationChannel;
import com.payfleet.model.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Notification Template Repository
 */
@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTemplateTypeAndChannelAndIsActive(
            String templateType, NotificationChannel channel, Boolean isActive);

    List<NotificationTemplate> findByTemplateTypeAndIsActive(String templateType, Boolean isActive);

    List<NotificationTemplate> findByChannelAndIsActive(NotificationChannel channel, Boolean isActive);

    List<NotificationTemplate> findByIsActive(Boolean isActive);
}
