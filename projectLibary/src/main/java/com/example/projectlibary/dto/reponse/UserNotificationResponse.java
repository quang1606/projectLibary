package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationResponse {
     Long id;
     String message;
     NotificationType type;
     boolean isRead;
     LocalDateTime sentAt;
     String relatedEntityType;
     Long relatedEntityId;
}
