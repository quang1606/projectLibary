package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.BookReservationStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Builder
@Value
public class BookReservationResponse {
     Long id;
     LocalDateTime reservedAt;
     BookReservationStatus status;
     LocalDate availableUntil;
     Integer queuePosition;

    // --- Dữ liệu được làm phẳng ---
     Long userId;
     String userFullName;
     String userStudentId;

     Long bookId;
     String bookTitle;
}
