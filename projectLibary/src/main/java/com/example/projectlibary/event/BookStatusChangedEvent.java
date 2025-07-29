package com.example.projectlibary.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
public class BookStatusChangedEvent {
    private Long userId;
    private Long bookId;
    private LocalDateTime addedAt;
}
