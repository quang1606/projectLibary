package com.example.projectlibary.event;

import com.example.projectlibary.common.ReturnCondition;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
public class FineIssuedEvent {
    private Long userId;
    private String userEmail;
    private String userFullName; // Tên để email thêm phần cá nhân hóa

    // --- Thông tin về lượt mượn gây ra phạt ---
    private Long loanId;
    private String bookTitle;
    private String bookCopyNumber;

    // --- Thông tin về khoản phạt ---
    private Long paymentId; // ID của bản ghi Payment đã được tạo
    private BigDecimal fineAmount;
    private ReturnCondition reason; // Lý do phạt (hỏng nhẹ, mất,...)
    private String librarianNotes; // Ghi chú của thủ thư

    // --- Thông tin bổ sung ---
    private LocalDateTime eventTimestamp; // Thời điểm sự kiện xảy ra
}
