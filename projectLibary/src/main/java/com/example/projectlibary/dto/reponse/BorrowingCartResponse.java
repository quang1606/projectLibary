package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.BorrowingCartStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@Builder
public class BorrowingCartResponse {
    private Long cartId; // ID của giỏ hàng

    private Long userId; // ID của người dùng sở hữu giỏ hàng

    private BorrowingCartStatus status;

    private LocalDateTime expiresAt; // Thời điểm hết hạn
    private String confirmationCode;
    private LocalDateTime confirmationCodeExpiresAt;
    private String qrCodeImageBase64;

    // Dữ liệu quan trọng nhất: danh sách các sách trong giỏ
    private List<BorrowingCartItemResponse> items;

    // (Tùy chọn) Thêm các thông tin tiện ích
    private int totalItems;
}
