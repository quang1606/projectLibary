package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.common.PaymentMethod;
import com.example.projectlibary.common.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Value

public class PaymentResponse {
     Long id;
     BigDecimal amount;
     LocalDateTime paymentDate;
     PaymentMethod paymentMethod;
     PaymentStatus status;
     String transactionId;
     String note;

    // --- Dữ liệu được làm phẳng ---
     Long userId;
     String userFullName;
     Long loanId; // ID của lượt mượn gây ra khoản phạt (nếu có)
     String processedByLibrarianName; // Tên thủ thư xử lý
}
