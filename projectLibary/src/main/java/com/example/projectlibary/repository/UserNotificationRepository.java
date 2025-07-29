package com.example.projectlibary.repository;

import com.example.projectlibary.dto.reponse.UserNotificationResponse;
import com.example.projectlibary.model.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {


    Page<UserNotification> findByUser_Id(Long id, Pageable pageable);
}