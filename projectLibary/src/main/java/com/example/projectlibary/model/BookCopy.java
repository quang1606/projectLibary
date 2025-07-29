package com.example.projectlibary.model;

import com.example.projectlibary.common.BookCopyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "book_copies")
@EntityListeners(AuditingEntityListener.class)
public class BookCopy extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookcopies_book"))
    private Book book;

    @Column(name = "copy_number", length = 50, nullable = false, unique = true)
    private String copyNumber; // Mã định danh duy nhất cho bản sao, VD: ISBN-001

    @Column(name = "qr_code", length = 255, nullable = false, unique = true)
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookCopyStatus status = BookCopyStatus.AVAILABLE; // Trạng thái của bản sao

    @Column(name = "location", length = 100)
    private String location; // Vị trí cố định trên kệ

    @Column(name = "added_date")
    private LocalDate addedDate;



    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", updatable = false, foreignKey = @ForeignKey(name = "fk_bookcopies_created_by",
            foreignKeyDefinition = "FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private User createdBy;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", foreignKey = @ForeignKey(name = "fk_bookcopies_updated_by",
            foreignKeyDefinition = "FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private User updatedBy;

    @OneToMany(mappedBy = "bookCopy")
    private Set<BookLoan> bookLoans;

}

