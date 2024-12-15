package com.example.push_notification.controller;

import com.example.push_notification.entity.NotificationRequest;
import com.example.push_notification.service.PushNotificationService;
import com.windowsazure.messaging.NotificationHubsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class PushNotificationController {
    private final PushNotificationService pushNotificationService;

    
    @SuppressWarnings("rawtypes")
    @PostMapping("/send")
    public ResponseEntity sendPushNotification(@RequestBody NotificationRequest request) throws ExecutionException, InterruptedException {
        String response = pushNotificationService.sendPushNotificationFirebase(request);
        return ResponseEntity.ok(response);
    }
    @SuppressWarnings("rawtypes")
    @PostMapping("/send/azure")
    public ResponseEntity sendPushNotificationAzure(@RequestBody NotificationRequest request) throws ExecutionException, InterruptedException, NotificationHubsException, IOException {
        String response = pushNotificationService.sendPushNotificationWithAzure(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/healthz")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Health check passed");
    }

    @PostMapping("/notify-all")
    public ResponseEntity<String> notifyAll(@RequestBody NotificationRequest request){
        String response = pushNotificationService.notifyAll(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/schedule")
    public ResponseEntity<String> scheduleNotification(@RequestBody NotificationRequest request,
                                                       @RequestParam String sendTime) {
        LocalDateTime scheduledTime = LocalDateTime.parse(sendTime); // Định dạng: "yyyy-MM-ddTHH:mm:ss"
        if(scheduledTime.isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Scheduled time must be in the future");
        }
        String response = pushNotificationService.scheduleNotification(request, scheduledTime);
        return ResponseEntity.ok(response);
    }
}
