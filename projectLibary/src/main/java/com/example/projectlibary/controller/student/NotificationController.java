package com.example.projectlibary.controller.student;

import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.reponse.UserNotificationResponse;
import com.example.projectlibary.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    @GetMapping("")
    public ResponseEntity<ResponseData<PageResponse<UserNotificationResponse>>> getNotification(@RequestParam (defaultValue = "0") int page,
                                                                                                @RequestParam(defaultValue = "10") int size){
        PageResponse<UserNotificationResponse> userNotificationResponse = notificationService.getAllNotifications(page,size);
        ResponseData<PageResponse<UserNotificationResponse>> responseData = new ResponseData<>(200,"Success",userNotificationResponse);
        return ResponseEntity.ok(responseData);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<UserNotificationResponse>> getNotificationById(@PathVariable("id") long id){
        UserNotificationResponse userNotificationResponse = notificationService.getNotificationById(id);
        ResponseData<UserNotificationResponse> responseData = new ResponseData<>(200,"Success",userNotificationResponse);
        return ResponseEntity.ok(responseData);
    }
}
