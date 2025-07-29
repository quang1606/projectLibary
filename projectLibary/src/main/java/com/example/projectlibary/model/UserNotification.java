package com.example.projectlibary.model;

import com.example.projectlibary.common.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "user_notifications")
@EntityListeners(AuditingEntityListener.class)
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_usernotifs_user"))
    private User user;

    @Lob
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type = NotificationType.GENERAL;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false; // 0: Chưa đọc, 1: Đã đọc

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // VD: book, loan, reservation

    @Column(name = "related_entity_id")
    private Long relatedEntityId; // ID của thực thể liên quan

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

}