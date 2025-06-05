package com.example.projectlibary.repository;

import com.example.projectlibary.model.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
}