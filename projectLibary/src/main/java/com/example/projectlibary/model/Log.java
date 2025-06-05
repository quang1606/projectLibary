package com.example.projectlibary.model;

import com.example.projectlibary.common.LogLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "logs")
public class Log { // Does not extend AbstractEntity to manage its own ID and created_at specifically if different
    // Or adjust AbstractEntity if all tables follow its ID/timestamp pattern.
    // For this case, let's make it specific to 'logs' table structure.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_logs_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE"))
    private User user; // User thực hiện hành động

    @Column(name = "action", length = 255, nullable = false)
    private String action; // VD: USER_LOGIN, BOOK_ADDED

    @Lob
    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // Thông tin chi tiết

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private LogLevel level = LogLevel.INFO;

    @Column(name = "module", length = 100)
    private String module; // Module xảy ra hành động

    @Column(name = "created_at", updatable = false) // Named 'created_at' as per SQL, not 'created_at_ts' unless intended
    private LocalDateTime createdAtTs; // Renamed to avoid confusion if this Log entity does NOT extend AbstractEntity.
    // If it DOES extend, then this field is inherited.
    // SQL has `created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP`

    @PrePersist
    protected void onPrePersist() {
        if (this.createdAtTs == null) {
            this.createdAtTs = LocalDateTime.now();
        }
    }
}