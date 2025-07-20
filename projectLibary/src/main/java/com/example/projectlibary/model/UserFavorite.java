package com.example.projectlibary.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "user_favorites",
        // Ánh xạ ràng buộc UNIQUE từ database vào entity để đảm bảo tính nhất quán
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_book_favorite",
                        columnNames = {"user_id", "book_id"}
                )
        }
)
public class UserFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Mối quan hệ với User ---
    // Nhiều lượt "yêu thích" có thể thuộc về một User
    @ManyToOne(fetch = FetchType.LAZY) // LAZY: chỉ load User khi thực sự cần, tốt cho hiệu năng
    @JoinColumn(name = "user_id", nullable = false) // Nối với cột 'user_id' trong bảng user_favorites
    private User user;

    // --- Mối quan hệ với Book ---
    // Nhiều lượt "yêu thích" có thể thuộc về một Book
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false) // Nối với cột 'book_id'
    private Book book;

    @CreationTimestamp // Tự động gán thời gian hiện tại khi bản ghi được tạo
    @Column(name = "favorited_at", nullable = false, updatable = false)
    private Instant favoritedAt;


}