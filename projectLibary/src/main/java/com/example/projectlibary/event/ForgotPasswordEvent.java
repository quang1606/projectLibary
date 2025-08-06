package com.example.projectlibary.event;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordEvent {
    private Long UserId;
    private String Email;
    private String appUrl;
}
