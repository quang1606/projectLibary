package com.example.projectlibary.common;

public enum NotificationType {
    RESERVATION_AVAILABLE,               // Thông báo sách đặt trước đã sẵn sàng
    DUE_DATE_REMINDER,                   // Nhắc nhở sắp đến hạn trả sách
    OVERDUE_NOTICE,                      // Thông báo quá hạn trả sách
    NEW_BOOK,                            // Thông báo sách mới
    EVENT,                               // Thông báo sự kiện
    GENERAL,                             // Thông báo chung
    BORROW_EXPIRED,                      // Thông báo mượn sách đã hết hạn
    RETURN_CONFIRMED,                    // Xác nhận đã trả sách
    FINE_ISSUED,                         // Thông báo phạt đã được ghi nhận

}
