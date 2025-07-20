package com.example.projectlibary.model;
import com.example.projectlibary.common.BookLoanStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "book_loans")
@EntityListeners(AuditingEntityListener.class)
public class BookLoan extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookloans_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookloans_copy",
            foreignKeyDefinition = "FOREIGN KEY (book_copy_id) REFERENCES book_copies(id) ON DELETE RESTRICT ON UPDATE CASCADE"))
    private BookCopy bookCopy;

    @Column(name = "borrowed_at", nullable = false)
    private LocalDateTime borrowedAt; // Thời điểm Thủ thư xác nhận mượn lần 2 thành công

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate; // Ngày hết hạn trả sách

    @Column(name = "student_initiated_return_at")
    private LocalDateTime studentInitiatedReturnAt; // Thời điểm SV quét QR/thao tác trả sách ban đầu

    @Column(name = "librarian_confirmed_return_at")
    private LocalDateTime librarianConfirmedReturnAt; // Thời điểm Thủ thư xác nhận trả sách OK/Hỏng

    @Column(name = "fine_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO; // Tiền phạt

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookLoanStatus status = BookLoanStatus.BORROWED; // Cập nhật khi Thủ thư xác nhận trả/báo mất

    @Lob
    @Column(name = "return_condition", columnDefinition = "TEXT")
    private String returnCondition; // Ghi chú tình trạng sách khi Thủ thư xác nhận trả

    @Column(name = "return_shelf_location", length = 100)
    private String returnShelfLocation; // Vị trí kệ SV quét khi trả (nếu có)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "librarian_id", foreignKey = @ForeignKey(name = "fk_bookloans_librarian",
            foreignKeyDefinition = "FOREIGN KEY (librarian_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private User librarian; // Thủ thư xử lý xác nhận mượn/trả

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", updatable = false, foreignKey = @ForeignKey(name = "fk_bookloans_created_by",
            foreignKeyDefinition = "FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private User createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", foreignKey = @ForeignKey(name = "fk_bookloans_updated_by",
            foreignKeyDefinition = "FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private User updatedBy;

    @OneToMany(mappedBy = "loan")
    private Set<Payment> payments;

    @OneToOne(mappedBy = "fulfilledLoan")
    private BookReservation reservationFulfilled;

}