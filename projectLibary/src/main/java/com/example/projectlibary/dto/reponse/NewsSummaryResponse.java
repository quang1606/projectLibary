package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.NewsStatus;
import lombok.*;

import java.time.LocalDateTime;
@Builder
@Value
public class NewsSummaryResponse {
     Long id;
     String title;
     NewsStatus status;
     LocalDateTime createdAt;

    // --- Dữ liệu được làm phẳng ---
     String createdByFullName;

}
