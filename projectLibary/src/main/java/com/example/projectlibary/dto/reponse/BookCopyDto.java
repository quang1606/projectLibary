package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.model.BookCopy;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link BookCopy}
 */
@Builder
@Value
public class BookCopyDto implements Serializable {
    private Long id;
    private BookSummaryResponse book; // Chỉ thông tin tóm tắt của sách
    private String copyNumber;
    private String qrCode;
    private BookCopyStatus status;
    private String location;
    private LocalDate addedDate;
    private UserSummaryResponse createdBy;
    private UserSummaryResponse updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long pendingBorrowId; // ID của yêu cầu mượn chờ nếu có
}