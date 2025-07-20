package com.example.projectlibary.common;

public enum BookCopyStatus {
    AVAILABLE, // Sách có sẵn, chưa được mượn.
    RESERVED, // Đang được giữ cho người đặt trước.
    PENDING,      // Đang chờ xác nhận cho mượn
    BORROWED, // Sách đã được mượn.
    LOST, // Sách bị mất.
    DISCARDED,   // Đã được thanh lý/loại bỏ khỏi thư viện
    DAMAGED,     // Bị hỏng

}

