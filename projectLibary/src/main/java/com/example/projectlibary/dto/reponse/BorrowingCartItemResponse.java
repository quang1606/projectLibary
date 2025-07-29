package com.example.projectlibary.dto.reponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BorrowingCartItemResponse {
    private Long cartItemId; // ID của chính item trong giỏ

    // --- Thông tin được làm phẳng từ BookCopy và Book ---
    private Long bookCopyId;
    private String copyNumber;

    private Long bookId;
    private String bookTitle;
    private String bookThumbnailUrl; // Ảnh bìa sách

    private LocalDateTime addedAt; // Thời điểm thêm vào giỏ
}
