package com.example.projectlibary.event;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookStatusChangedEvent {
    private Long userId;
    private Long bookId;
    private LocalDateTime addedAt;
}
