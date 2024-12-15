package com.example.push_notification.repository;
import com.example.push_notification.entity.Key;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.push_notification.entity.User;

@Repository
public interface KeyRepository extends JpaRepository<Key, Long> {
    @Query("SELECT id, notificationKey from Key where notificationKey is not null and notificationKey != ''")
    List<Object[]> getAll();
}