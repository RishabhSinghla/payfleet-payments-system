package com.payfleet.repository;

import com.payfleet.model.NotificationChannel;
import com.payfleet.model.NotificationLog;
import com.payfleet.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification Log Repository
 */
@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    @Query("SELECT n FROM NotificationLog n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId,
                                                           @Param("limit") int limit);

    List<NotificationLog> findByReferenceIdOrderByCreatedAtDesc(String referenceId);

    List<NotificationLog> findByStatusAndNextRetryAtBefore(NotificationStatus status, LocalDateTime dateTime);

    long countByStatus(NotificationStatus status);

    long countByChannel(NotificationChannel channel);

    long countByUserIdAndStatus(String userId, NotificationStatus status);

    List<NotificationLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
