package com.example.push_notification.service;

import com.example.push_notification.entity.NotificationRequest;
import com.example.push_notification.entity.ScheduledNotification;
import com.example.push_notification.entity.User;
import com.example.push_notification.repository.ScheduledNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final ScheduledNotificationRepository scheduledNotificationRepository;
    private final PushNotificationService pushNotificationService;
    private final UserService userService;

    @Scheduled(fixedRate = 30000)
    public void processScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledNotification> notifications = scheduledNotificationRepository.findBySentFalseAndSendTimeBefore(now);

        for (ScheduledNotification notification : notifications) {
            try {
                if (notification.getToken() == null || notification.getToken().isEmpty()) {
                    List<String> tokens = userService.getUsers().stream().map(User::getToken).filter(token -> token != null && token.length() > 20).toList();
                    for (String token : tokens) {
                        try {
                            NotificationRequest request = NotificationRequest.builder()
                                    .title(notification.getTitle())
                                    .body(notification.getBody())
                                    .data(notification.getData())
                                    .token(token)
                                    .build();
                            pushNotificationService.sendPushNotificationFirebase(request);
                        } catch (Exception e) {
                            log.error("Error sending notification to user with token {}: {}", token, e.getMessage());
                        }
                    }
                } else {
                    try {
                        NotificationRequest request = NotificationRequest.builder()
                                .title(notification.getTitle())
                                .body(notification.getBody())
                                .data(notification.getData())
                                .token(notification.getToken())
                                .build();
                        pushNotificationService.sendPushNotificationFirebase(request);
                    } catch (Exception e) {
                        log.error("Error sending notification (ID: {}): {}", notification.getId(), e.getMessage());
                    }
                }
                notification.setSent(true);
                scheduledNotificationRepository.save(notification);
            } catch (Exception e) {
                log.error("Error processing scheduled notification (ID: {}): {}", notification.getId(), e.getMessage());
            }
        }
    }

}
