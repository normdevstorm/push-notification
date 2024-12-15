package com.example.push_notification.repository;

import com.example.push_notification.entity.ScheduledNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledNotificationRepository extends JpaRepository<ScheduledNotification, Long> {
    List<ScheduledNotification> findBySentFalseAndSendTimeBefore(LocalDateTime now);
}

