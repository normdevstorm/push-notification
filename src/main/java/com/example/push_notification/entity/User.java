package com.example.push_notification.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private long id;
    private String token;
}
