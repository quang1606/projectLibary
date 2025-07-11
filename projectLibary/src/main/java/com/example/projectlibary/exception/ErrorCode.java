package com.example.projectlibary.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum ErrorCode {
    // === Lỗi chung & Xác thực / Phân quyền (Nhóm 1xxx) ===
    //Trả về lỗi 500 chung
    UNCATEGORIZED_EXCEPTION(999,"Uncategorized  error", HttpStatus.INTERNAL_SERVER_ERROR),
    //Kích hoạt khi dữ liệu đầu vào từ client không hợp lệ (lỗi từ @Valid, @NotBlank, etc.)
    VALIDATION_EXCEPTION(1001,"Invalid data", HttpStatus.BAD_REQUEST),
    //Dùng khi người dùng truy cập tài nguyên yêu cầu đăng nhập mà không có token hợp lệ
    AUTHENTICATION_EXCEPTION(1002,"Authentication ", HttpStatus.UNAUTHORIZED),
     // Dùng khi người dùng đã xác thực (đã biết là ai) nhưng không có quyền
    ACCESS_DENIED(1003,"Access denied", HttpStatus.FORBIDDEN),
    //Dùng khi tìm kiếm một người dùng (qua ID, username) không tồn tại trong cơ sở dữ liệu,
    USER_NOT_FOUND(1004, "User not found", HttpStatus.NOT_FOUND),
    // Dùng trong API đăng ký khi username hoặc email đã được sử dụng bởi một tài khoản khác.
    USER_ALREADY_EXIST(1005, "User already exist", HttpStatus.CONFLICT),

    // === Lỗi liên quan đến Sách, Tác giả, Thể loại (Nhóm 2xxx) ===
    BOOK_NOT_FOUND(1006, "Book not found", HttpStatus.NOT_FOUND),
    BOOK_COPY_NOT_FOUND(1007, "Book copy not found", HttpStatus.NOT_FOUND),
    ISBN_ALREADY_EXIST(1008, "ISBN already exist", HttpStatus.CONFLICT),
    QR_CODE_ALREADY_EXIST(1009, "QR code already exist", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND(1010, "Category not found", HttpStatus.NOT_FOUND),
    AUTHOR_NOT_FOUND(1011, "Author not found", HttpStatus.NOT_FOUND),
    POSR_NOT_FOUND(1012, "Position not found", HttpStatus.NOT_FOUND),

    // === Lỗi nghiệp vụ Mượn/Trả sách (Nhóm 3xxx) ===
    BOOK_COPY_NOT_AVAILABLE(3001, "This book copy is not available for borrowing", HttpStatus.BAD_REQUEST),
    BORROWING_LIMIT_REACHED(3002, "User has reached the borrowing limit", HttpStatus.BAD_REQUEST),
    /**
     * Quy tắc nghiệp vụ: Hệ thống chặn không cho người dùng mượn sách mới vì họ đang có
     * ít nhất một cuốn sách quá hạn trả.
     */
    USER_HAS_OVERDUE_BOOKS(3003, "Cannot borrow, user has overdue books", HttpStatus.FORBIDDEN),

    BOOK_LOAN_NOT_FOUND(3004, "Book loan record not found", HttpStatus.NOT_FOUND),
    /**
     * Kích hoạt khi cố gắng thực hiện hành động 'trả' một bản sao sách không ở trong
     * trạng thái đã được mượn bởi người dùng đó.
     */
    INVALID_RETURN_STATE(3005, "Book copy is not in a state that can be returned", HttpStatus.BAD_REQUEST),
    PENDING_BORROW_NOT_FOUND(3006, "Pending borrow request not found or expired", HttpStatus.NOT_FOUND),

    // === Lỗi nghiệp vụ Đặt trước sách (Nhóm 4xxx) ===

    //Người dùng cố gắng đặt trước một đầu sách mà họ đã có một yêu cầu đặt trước khác
    DUPLICATE_RESERVATION(4001, "User already has an active reservation for this book", HttpStatus.CONFLICT),

    // Quy tắc nghiệp vụ: không cho phép đặt trước sách khi vẫn còn bản sao 'AVAILABLE' trên kệ
    CANNOT_RESERVE_AVAILABLE_BOOK(4002, "This book has available copies and cannot be reserved", HttpStatus.BAD_REQUEST),
    //Dùng khi tra cứu một yêu cầu đặt trước (qua ID) không tìm thấy
    RESERVATION_NOT_FOUND(4003, "Reservation not found", HttpStatus.NOT_FOUND),

    // === Lỗi liên quan đến Token (Nhóm 5xxx) ===
    REFRESH_TOKEN_NOT_FOUND(5001, "Refresh token is not in database!", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_EXPIRED(5002, "Refresh token was expired. Please make a new sign-in request", HttpStatus.FORBIDDEN);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
