package com.example.projectlibary.event;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationEvent {
    private String event;
    private Long userId;
    private String email;
    private String appUrl;
}
