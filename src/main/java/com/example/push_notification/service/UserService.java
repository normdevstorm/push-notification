package com.example.push_notification.service;

import com.example.push_notification.entity.User;
import com.example.push_notification.repository.KeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final KeyRepository keyRepository;

    @Transactional
    public List<User> getUsers(){
        List<Object[]> results = keyRepository.getAll();
        List<User> users = new ArrayList<>();

        for (Object[] result : results) {
            Long id = (Long) result[0];
            String notificationKey = (String) result[1];
            if(notificationKey == null || notificationKey.isEmpty()){
                continue;
            }
            users.add(User.builder().id(id).token(notificationKey).build());
        }
        return users;
    }
}
