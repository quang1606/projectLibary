package com.example.projectlibary.dto.reponse;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor

public class BookReviewResponse {
     Long id;
     int rating;
     String comment;
     LocalDateTime reviewDate;

    // Thông tin người đánh giá (dùng DTO đơn giản)
     Long userId;
     String userFullName;
     String userAvatar;
}
