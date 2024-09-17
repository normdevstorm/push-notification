package com.example.push_notification.controller;

import com.example.push_notification.entity.NotificationRequest;
import com.example.push_notification.service.PushNotificationService;
import com.windowsazure.messaging.NotificationHubsException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("")
public class PushNotificationController {
    private PushNotificationService pushNotificationService;

    public PushNotificationController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }
    @PostMapping("/send")
    public ResponseEntity sendPushNotification(@RequestBody NotificationRequest request) throws ExecutionException, InterruptedException {
        String response = pushNotificationService.sendPushNotificationFirebase(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/send/azure")
    public ResponseEntity sendPushNotificationAzure(@RequestBody NotificationRequest request) throws ExecutionException, InterruptedException, NotificationHubsException, IOException {
        String response = pushNotificationService.sendPushNotificationWithAzure(request);
        return ResponseEntity.ok(response);
    }

}
