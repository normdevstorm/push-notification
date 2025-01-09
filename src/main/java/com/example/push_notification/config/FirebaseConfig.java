package com.example.push_notification.config;
import com.example.push_notification.entity.NotificationRequest;
import com.example.push_notification.service.PushNotificationService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${app.firebase-configuration-file}")
    private String firebaseConfigPath;
    private final PushNotificationService pushNotificationService;
    //TODO: set hard value for now
    private String token= "c3j_uTtvRPSxik9VAHpjXH:APA91bG-5jNwLM7ggs3h73j8l1Qr3y5Cs-8oZdRF-ersHzVuncA-eB4sv22qWKtfpDAAn23geClDbRVPpppyAQ-laZorywRavku6448IXhGIjLVcc28UmrY";
    private boolean isFirebaseActive;
    private boolean isAlertSendingActive;
    private byte previousAlertLevel;
    private byte currentAlertLevel;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @PostConstruct
    public void firebaseInit() {
        try {
            FirebaseOptions options = new FirebaseOptions
                    .Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ClassPathResource(firebaseConfigPath).getInputStream()))
                    .setDatabaseUrl("https://cloud-message-test-1d41b-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .build();
            FirebaseApp.initializeApp(options);
            isFirebaseActive = true;
        } catch (Exception e) {
            logger.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            return;
        }

        // Start threads for listening and alert sending
        executorService.execute(this::startListeningForData);
        executorService.execute(this::startSendingAlerts);
    }

    /**
     * Thread to listen for data changes in Firebase Realtime Database.
     */
    private void startListeningForData() {
        DatabaseReference ref;
        try {
            ref = FirebaseDatabase.getInstance().getReference("/alerts");
        } catch (Exception e) {
            logger.error("Error creating DatabaseReference: {}", e.getMessage(), e);
            return;
        }

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        boolean levelOneDetected = Boolean.TRUE.equals(dataSnapshot.child("level_1").child("detected").getValue(Boolean.class));
                        boolean levelTwoDetected = Boolean.TRUE.equals(dataSnapshot.child("level_2").child("detected").getValue(Boolean.class));

                        if (levelTwoDetected) {
                            isAlertSendingActive = previousAlertLevel != 2;
                            currentAlertLevel = 2;
                            logger.info("Level 2 alert detected");
                        } else if (levelOneDetected) {
                            isAlertSendingActive = previousAlertLevel != 1;
                            currentAlertLevel = 1;
                            logger.info("Level 1 alert detected");
                        } else {
                            isAlertSendingActive = false;
                            currentAlertLevel = 0;
                            logger.info("No alert detected");
                        }
                    } catch (Exception e) {
                        logger.error("Error processing data snapshot: {}", e.getMessage(), e);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                logger.error("Listener was cancelled: {}", databaseError.getMessage());
            }
        });
    }

    /**
     * Thread to handle sending alerts.
     */
    private void startSendingAlerts() {
        while (true) {
            try {
                // Simulating an alert sending process
                Thread.sleep(2000); // Simulate delay for alert sending
                if(isFirebaseActive && isAlertSendingActive) {
                    if (currentAlertLevel == 1) {
                        pushNotificationService.sendPushNotificationFirebase(NotificationRequest.builder().title("Dubious Alert").body("Detected dubious object in front of your door").token(token).build());
                        logger.info("Sending Level 1 alert...");
                        // Here you would include logic for sending Level 1 alerts, e.g., to a notification service.
                    } else if (currentAlertLevel == 2) {
                        pushNotificationService.sendPushNotificationFirebase(NotificationRequest.builder().title("Dangerous Alert").body("Detected dangerous object in front of your door").token(token).build());
                        logger.info("Sending Level 2 alert...");
                        // Here you would include logic for sending Level 2 alerts, e.g., to a notification service.
                    }
                    isAlertSendingActive = false;
                    previousAlertLevel = currentAlertLevel;
                }
                logger.info("Sending alert...");
                // Here you would include logic for sending alerts, e.g., to a notification service.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Alert sending thread interrupted: {}", e.getMessage());
                break;
            } catch (Exception e) {
                logger.error("Error while sending alert: {}", e.getMessage(), e);
            }
        }
    }
}
