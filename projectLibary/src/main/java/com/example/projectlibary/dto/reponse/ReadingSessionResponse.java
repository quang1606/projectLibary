package com.example.projectlibary.dto.reponse;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Value
public class ReadingSessionResponse {
     Long id;
     LocalDateTime startTime;
     LocalDateTime endTime;
     long durationInSeconds; // Một trường được tính toán, rất hữu ích

    // --- Dữ liệu được làm phẳng từ các Entity liên quan ---
     Long bookId;
     String bookTitle;
     String bookIsbn;

     Long userId; // Có thể không cần nếu API chỉ trả về phiên đọc của người dùng hiện tại
     String username;
}

