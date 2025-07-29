package com.example.projectlibary.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(999, "Lỗi chưa được phân loại", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_EXCEPTION(1001, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_EXCEPTION(1002, "Lỗi xác thực", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(1003, "Truy cập bị từ chối", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(1004, "Người dùng không tìm thấy", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXIST(1005, "Người dùng đã tồn tại", HttpStatus.CONFLICT),
    REVIEW_ALREADY_EXISTS(1006, "Đánh giá đã tồn tại", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND(107, "Đánh giá không tìm thấy", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_ERROR(108, "Lỗi tải lên tệp", HttpStatus.INTERNAL_SERVER_ERROR),
    BOOK_NOT_DELETED(109,"Sách không ở trạng thái bị xóa" , HttpStatus.BAD_REQUEST ),
    NOTIFICATION_NOT_FOUND(110,"Không tìm thấy thông báo", HttpStatus.NOT_FOUND ),
            // === Lỗi liên quan đến Sách, Tác giả, Thể loại (Nhóm 2xxx) ===
    BOOK_NOT_FOUND(2001, "Sách không tìm thấy", HttpStatus.NOT_FOUND),
    BOOK_COPY_NOT_FOUND(2002, "Bản sao sách không tìm thấy", HttpStatus.NOT_FOUND),
    ISBN_ALREADY_EXIST(2003, "ISBN đã tồn tại", HttpStatus.CONFLICT),
    QR_CODE_ALREADY_EXIST(2004, "QR code đã tồn tại", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND(2005, "Thể loại không tìm thấy", HttpStatus.NOT_FOUND),
    AUTHOR_NOT_FOUND(2006, "Tác giả không tìm thấy", HttpStatus.NOT_FOUND),
    POST_NOT_FOUND(2007, "Vị trí không tìm thấy", HttpStatus.NOT_FOUND),
    AUTHOR_ALREADY_EXISTS(2008,"Tác giả đã tồn tại" ,HttpStatus.CONFLICT),
     USERNAME_ALREADY_EXIST(2009,"Tên tài khoản dã tồn tại" ,HttpStatus.CONFLICT ),

            // === Lỗi nghiệp vụ Mượn/Trả sách (Nhóm 3xxx) ===
    BOOK_COPY_NOT_AVAILABLE(3001, "Bản sao sách này không có sẵn để mượn", HttpStatus.BAD_REQUEST),
    BORROWING_LIMIT_REACHED(3002, "Người dùng đã đạt giới hạn mượn sách", HttpStatus.BAD_REQUEST),
    USER_HAS_OVERDUE_BOOKS(3003, "Không thể mượn sách, người dùng có sách quá hạn", HttpStatus.FORBIDDEN),
    BOOK_LOAN_NOT_FOUND(3004, "Hồ sơ mượn sách không tìm thấy", HttpStatus.NOT_FOUND),
    BORROWING_CART_NOT_FOUND(3005,"Không tìm thấy giỏ hàng" ,HttpStatus.NOT_FOUND ),
    BORROWING_CART_IS_EMPTY(3006,"Giỏ hàng đang trống" , HttpStatus.NOT_FOUND),
    ITEM_NOT_IN_CART(3006,"Không tìm thấy sách trong giỏ hàng", HttpStatus.NOT_FOUND),
    SCANNED_AND_CONFIRMED(3007,"Sách đã được quét qr rồi" ,HttpStatus.BAD_REQUEST ),
    NO_ITEMS_VERIFIED(3008,"Không có sách để xác nhận" ,HttpStatus.NOT_FOUND ),
    /**
     * Kích hoạt khi cố gắng thực hiện hành động 'trả' một bản sao sách không ở trong
     * trạng thái đã được mượn bởi người dùng đó.
     */
    INVALID_RETURN_STATE(3005, "Bản sao sách không ở trạng thái có thể trả lại", HttpStatus.BAD_REQUEST),
    BORROWING_CART_EXPIRED(3006, "Yêu cầu mượn  đã hết hạn", HttpStatus.NOT_FOUND),
    // Lỗi khi xóa sách
    CANNOT_DELETE_COPY_BORROWED(3007, "Không thể xóa bản sao sách đang được mượn", HttpStatus.CONFLICT),
    CANNOT_DELETE_COPY_RESERVED(3008, "Không thể xóa bản sao sách đã được đặt trước cho người dùng", HttpStatus.CONFLICT),
    CANNOT_DELETE_COPY_PENDING(3009, "Không thể xóa bản sao sách có yêu cầu mượn đang chờ xử lý", HttpStatus.CONFLICT),


    // === Lỗi nghiệp vụ Đặt trước sách (Nhóm 4xxx) ===
    DUPLICATE_RESERVATION(4001, "Người dùng đã có một yêu cầu đặt trước đang hoạt động cho sách này", HttpStatus.CONFLICT),
    CANNOT_RESERVE_AVAILABLE_BOOK(4002, "Sách này có bản sao có sẵn và không thể đặt trước", HttpStatus.BAD_REQUEST),
    RESERVATION_NOT_FOUND(4003, "Yêu cầu đặt trước không tìm thấy", HttpStatus.NOT_FOUND),

    // === Lỗi liên quan đến Token (Nhóm 5xxx) ===
    REFRESH_TOKEN_EXPIRED(5002, "Token làm mới đã hết hạn. Vui lòng yêu cầu đăng nhập lại", HttpStatus.UNAUTHORIZED),
    INVALID_VERIFICATION_TOKEN(5003,"Token xác minh không hợp lệ " ,HttpStatus.UNAUTHORIZED),
    EXPIRED_VERIFICATION_TOKEN(5004,"Token xác minh  đã hết hạn" ,HttpStatus.UNAUTHORIZED );


    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
