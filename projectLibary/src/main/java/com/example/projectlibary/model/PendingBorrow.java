package com.example.projectlibary.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "pending_borrows")
public class PendingBorrow implements Serializable { // AbstractEntity provides createdAt, updatedAt
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    // Override createdAt from AbstractEntity to match specific column name if needed,
    // but pending_borrows uses requested_at for the main timestamp.
    // AbstractEntity's createdAt will be the record creation time.

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_pendingborrows_copy"))
    private BookCopy bookCopy; // Mỗi bản copy chỉ có 1 yêu cầu chờ tại 1 thời điểm

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pendingborrows_user"))
    private User user; // Sinh viên yêu cầu mượn

    @Column(name = "requested_at") // Default is CURRENT_TIMESTAMP in DB
    private LocalDateTime requestedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Thời điểm hết hạn Countdown


}