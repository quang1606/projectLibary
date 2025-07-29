package com.example.projectlibary.common;

public enum BookCopyStatus {
    AVAILABLE, // Sách có sẵn, chưa được mượn.
    IN_CART,      // Đang chờ xác nhận cho mượn
    BORROWED, // Sách đã được mượn.
    LOST,       // Sách bị mất.
    DISCARDED,   // Đã được thanh lý/loại bỏ khỏi thư viện
    DAMAGED,    // Bị hỏng

}

