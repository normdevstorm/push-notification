package com.example.push_notification.service;

import com.example.push_notification.entity.NotificationRequest;
import com.example.push_notification.entity.ScheduledNotification;
import com.example.push_notification.entity.User;
import com.example.push_notification.repository.ScheduledNotificationRepository;
import com.google.firebase.messaging.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.windowsazure.messaging.FcmV1Notification;
import com.windowsazure.messaging.NotificationHub;
import com.windowsazure.messaging.NotificationHubsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushNotificationService {
    @Value("${azure.notification.hub.name}")
    private String hubName;

    @Value("${azure.notification.hub.connectionString}")
    private String connectionString;
    private final UserService userService;
    private final ScheduledNotificationRepository scheduledNotificationRepository;

    public String sendPushNotificationWithAzure(NotificationRequest request) throws NotificationHubsException, IOException {
        NotificationHub hub = new NotificationHub(connectionString, hubName);
        String bodyTemplate = String.format("{\"message\":{\"notification\": {\"title\": \"%s\",\"body\": \"%s\"},\"android\": {\"data\": {\"action\": \"%s\"}}}}", request.getTitle(), request.getBody(), request.getData().toString());//      com.windowsazure.messaging.Notification notification = com.windowsazure.messaging.Notification.createFcmNotification(fcmMessageage);
        hub.sendDirectNotification(new FcmV1Notification(bodyTemplate), request.getToken());
        return request.getBody();
    }
    public String sendPushNotificationFirebase(NotificationRequest request)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageToToken(request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(message);
        String response = sendAndGetResponse(message);
//        log.info("Sent message to token. Device token: " + request.getToken());
        return response;
    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }


    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setTag(topic).build()).build();
    }
    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }
    private Message getPreconfiguredMessageToToken(NotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).setToken(request.getToken())
                .build();
    }

    private Message.Builder getPreconfiguredMessageBuilder(NotificationRequest request) {
        AndroidConfig androidConfig = getAndroidConfig(request.getTitle());
        ApnsConfig apnsConfig = getApnsConfig(request.getTitle());
        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build();
        return Message.builder()
                .setApnsConfig(apnsConfig).setAndroidConfig(androidConfig).setNotification(notification);
    }

    @Transactional
    public String notifyAll(NotificationRequest request){
        try{
            List<User> users = userService.getUsers();
            for(User user: users){
                request.setToken(user.getToken());
                sendPushNotificationFirebase(request);
           }
            return "Notification sent to all users";
        } catch (Exception e){
            log.error("Error while sending notification to all users", e);
            return "Error while sending notification to all users";
        }
    }

    public String scheduleNotification(NotificationRequest request, LocalDateTime sendTime) {
        ScheduledNotification notification = ScheduledNotification.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .data(request.getData() != null ? request.getData().toString() : null)
                .token(request.getToken())
                .sendTime(sendTime)
                .build();

        scheduledNotificationRepository.save(notification);
        return "Notification scheduled successfully for " + sendTime;
    }


}
