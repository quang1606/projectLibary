package com.example.projectlibary.dto.reponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
public class WebSocketNotification {
    private String type; // "PAYMENT_SUCCESS"
    private String message;
    private LocalDateTime timestamp;
}
