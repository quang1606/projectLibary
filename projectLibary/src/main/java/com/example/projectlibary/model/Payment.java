package com.example.projectlibary.model;

import com.example.projectlibary.common.PaymentMethod;
import com.example.projectlibary.common.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "payments")
public class Payment extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payments_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE"))
    private User user; // SQL had BIGINTusers, corrected to user_id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", foreignKey = @ForeignKey(name = "fk_payments_loan",
            foreignKeyDefinition = "FOREIGN KEY (loan_id) REFERENCES book_loans(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private BookLoan loan; // Lượt mượn liên quan

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date") // Default CURRENT_TIMESTAMP in DB
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "transaction_id", length = 255, unique = true)
    private String transactionId; // ID giao dịch từ bên thứ 3

    @Lob
    @Column(name = "note", columnDefinition = "TEXT")
    private String note; // Ghi chú thanh toán

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", foreignKey = @ForeignKey(name = "fk_payments_processed_by",
            foreignKeyDefinition = "FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private User processedBy; // Thủ thư/Admin xử lý


}