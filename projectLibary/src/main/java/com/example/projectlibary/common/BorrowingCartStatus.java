package com.example.projectlibary.common;

public enum BorrowingCartStatus {
    ACTIVE,    // Đang trong thời gian đếm ngược
    EXPIRED,   // Đã hết hạn
    COMPLETED, // Đã được xác nhận mượn thành công
    CANCELLED,  // Sinh viên tự hủy
    PROCESSING,
    PENDING_CONFIRMATION
}
