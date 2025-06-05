package com.example.projectlibary.model;

import com.example.projectlibary.common.BookReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "book_reservations")
public class BookReservation extends AbstractEntity { // AbstractEntity provides createdAt, updatedAt

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookreservations_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookreservations_book"))
    private Book book; // Đặt trước theo đầu sách

    @Column(name = "reserved_at") // Default CURRENT_TIMESTAMP in DB
    private LocalDateTime reservedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookReservationStatus status = BookReservationStatus.PENDING;

    @Column(name = "available_until")
    private LocalDate availableUntil; // Hạn cuối để người dùng đến lấy sách

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt; // Thời điểm gửi thông báo sách sẵn sàng

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfilled_loan_id", foreignKey = @ForeignKey(name = "fk_bookreservations_loan",
            foreignKeyDefinition = "FOREIGN KEY (fulfilled_loan_id) REFERENCES book_loans(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private BookLoan fulfilledLoan; // Liên kết đến lượt mượn khi hoàn thành

    @Column(name = "queue_position")
    private Integer queuePosition; // Vị trí trong hàng đợi

    @Override
    protected void onPrePersist() {
        super.onPrePersist();
        if (this.reservedAt == null) {
            this.reservedAt = LocalDateTime.now();
        }
    }
}