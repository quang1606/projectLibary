package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.model.Book;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for {@link Book}
 */
@Builder
@Value
public class BookDto implements Serializable {
    private Long id; // Từ AbstractEntity
    private String title;
    private String description;
    private String isbn;
    private CategorySummaryResponse category; // DTO tóm tắt cho Category
    private String publisher;
    private Integer publicationYear;
    private String ebookUrl;
    private BigDecimal replacementCost;
    private UserSummaryResponse createdBy;
    private UserSummaryResponse updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<AuthorSummaryResponse> authors; // DTO tóm tắt cho Author
    private Integer availableCopiesCount; // Số lượng bản sao đang sẵn có
    private Double averageRating; // Đánh giá trung bình (tính toán)
    // private Set<BookReviewSummaryResponse> recentReviews; // Có thể thêm nếu cần
}