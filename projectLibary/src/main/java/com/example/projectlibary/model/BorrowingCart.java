package com.example.projectlibary.model;

import com.example.projectlibary.common.BorrowingCartSource;
import com.example.projectlibary.common.BorrowingCartStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "borrowing_carts", indexes = {
        // Index để worker tìm các giỏ hết hạn nhanh chóng
        @Index(name = "idx_carts_status_expires", columnList = "status, expires_at")
})
public class BorrowingCart extends AbstractEntity{
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = false,
            foreignKey = @ForeignKey(name = "fk_carts_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BorrowingCartStatus status = BorrowingCartStatus.ACTIVE;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Sử dụng LocalDateTime cho TIMESTAMP

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private BorrowingCartSource source;

    @Column(name = "confirmation_code", length = 20, nullable = true)
    private String confirmationCode;

    @Column(name = "confirmation_code_expires_at", nullable = true)
    private LocalDateTime confirmationCodeExpiresAt;
    // ...
    // Mối quan hệ một-nhiều với BorrowingCartItem
    // CascadeType.ALL: Khi một giỏ hàng được lưu/xóa, các item cũng vậy.
    // orphanRemoval = true: Nếu một item bị xóa khỏi Set này, nó cũng sẽ bị xóa khỏi CSDL.
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BorrowingCartItem> items = new HashSet<>();

    public void addItem(BorrowingCartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(BorrowingCartItem item) {
        items.remove(item);
        item.setCart(null);
    }
}
