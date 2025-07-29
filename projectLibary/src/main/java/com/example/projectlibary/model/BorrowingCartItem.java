package com.example.projectlibary.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "borrowing_cart_items")

public class BorrowingCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mối quan hệ nhiều-một với BorrowingCart
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cartitems_cart"))
    private BorrowingCart cart;

    // UNIQUE constraint được xử lý ở đây
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_cartitems_copy"))
    private BookCopy bookCopy;
    @Column(name = "is_verified_by_librarian", nullable = false)
    private boolean isVerifiedByLibrarian = false;
    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;
}
