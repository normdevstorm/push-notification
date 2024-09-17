package com.example.push_notification.entity;

import lombok.*;

import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class NotificationRequest implements Serializable {
    private String title;
    private Object data;
    private String body;
    private String token;
}
