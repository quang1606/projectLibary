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
    @Table(name = "book_reviews",
            uniqueConstraints = {
                    @UniqueConstraint(name = "uk_bookreviews_user_book", columnNames = {"user_id", "book_id"})
            }
          )
    public class BookReview implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", updatable = false, nullable = false)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookreviews_user"))
        private User user;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookreviews_book"))
        private Book book; // Đánh giá theo đầu sách

        @Column(name = "rating", nullable = false)
        // @Min(1) // Requires jakarta.validation
        // @Max(5) // Requires jakarta.validation
        private Integer rating; // Điểm đánh giá từ 1 đến 5

        @Lob
        @Column(name = "comment", columnDefinition = "TEXT")
        private String comment;

        @Column(name = "review_date") // Default CURRENT_TIMESTAMP in DB
        private LocalDateTime reviewDate;


       @PrePersist
        protected void onPrePersist() {

            if (this.reviewDate == null) {
                this.reviewDate = LocalDateTime.now(); // Set reviewDate specifically
            }
            // If review_date should be the same as AbstractEntity.createdAt, remove the specific reviewDate field
            // and map AbstractEntity.createdAt to "review_date" column
            // For now, assuming review_date is its own field with default.
        }
    }