package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.BookLoanStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Value
public class BookLoanResponse {
     Long id;
     LocalDateTime borrowedAt;
     LocalDate dueDate;
     LocalDateTime librarianConfirmedReturnAt;
     BigDecimal fineAmount;
     BookLoanStatus status;

    // --- Dữ liệu được làm phẳng từ các Entity liên quan ---
    // Thông tin người mượn
     Long userId;
     String userFullName;

    // Thông tin sách được mượn
     Long bookId;
     String bookTitle;
     String bookCopyNumber;

    // Thông tin thủ thư xử lý
     String librarianName;
}
