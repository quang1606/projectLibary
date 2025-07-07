package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.PaymentResponse;
import com.example.projectlibary.model.BookLoan;
import com.example.projectlibary.model.Payment;
import com.example.projectlibary.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        // Luôn kiểm tra null để đảm bảo an toàn
        if (payment == null) {
            return null;
        }

        // Lấy ra các đối tượng liên quan để tái sử dụng và kiểm tra null
        User user = payment.getUser();
        BookLoan loan = payment.getLoan();
        User processedBy = payment.getProcessedBy();

        // Sử dụng builder để tạo đối tượng PaymentResponse bất biến
        return PaymentResponse.builder()
                // Map các trường trực tiếp từ Payment
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .note(payment.getNote())

                // Map các trường "làm phẳng" từ User (người thanh toán)
                .userId(user != null ? user.getId() : null)
                .userFullName(user != null ? user.getFullName() : "N/A")

                // Map các trường "làm phẳng" từ BookLoan (lượt mượn liên quan)
                .loanId(loan != null ? loan.getId() : null)

                // Map các trường "làm phẳng" từ User (thủ thư xử lý)
                .processedByLibrarianName(processedBy != null ? processedBy.getFullName() : "Chưa xử lý")

                // Hoàn thành việc xây dựng đối tượng
                .build();
    }


    public List<PaymentResponse> toResponseList(List<Payment> payments) {
        if (payments == null) {
            return Collections.emptyList();
        }
        return payments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
