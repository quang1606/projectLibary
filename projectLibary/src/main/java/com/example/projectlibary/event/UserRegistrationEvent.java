package com.example.projectlibary.event;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationEvent {
    private Long userId;
    private String email;
    private String appUrl;
}
