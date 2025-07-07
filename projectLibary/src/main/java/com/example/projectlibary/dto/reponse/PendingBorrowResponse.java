package com.example.projectlibary.dto.reponse;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Value
public class PendingBorrowResponse {
     Long id;
     LocalDateTime requestedAt;
     LocalDateTime expiresAt; // Thời gian countdown

    // --- Dữ liệu được làm phẳng ---
     Long userId;
     String userFullName;
     String userStudentId;

     Long bookCopyId;
     String bookCopyNumber;
     String bookTitle; // Lấy từ bookCopy.getBook().getTitle()
}
