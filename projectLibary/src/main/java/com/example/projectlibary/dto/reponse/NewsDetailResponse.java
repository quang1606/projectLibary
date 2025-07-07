package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.NewsStatus;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Value
public class NewsDetailResponse {
     Long id;
     String title;
     String content; // Có nội dung đầy đủ
     NewsStatus status;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;

    // --- Dữ liệu được làm phẳng ---
     String createdByFullName;
     String updatedByFullName;
}
