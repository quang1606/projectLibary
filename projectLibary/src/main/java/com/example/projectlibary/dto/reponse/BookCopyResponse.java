package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.BookCopyStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Value
public class BookCopyResponse {
     Long id;
     String copyNumber;
     String qrCode;
     BookCopyStatus status;
     String location;
     LocalDate addedDate;

    // --- Dữ liệu được làm phẳng ---
     Long bookId;
     String bookTitle;
     String bookIsbn;
}
