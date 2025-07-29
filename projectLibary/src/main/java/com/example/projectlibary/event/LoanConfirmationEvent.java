package com.example.projectlibary.event;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanConfirmationEvent {
    private Long cartId;
    private Long userId;
    private LocalDateTime confirmationDate;


}
