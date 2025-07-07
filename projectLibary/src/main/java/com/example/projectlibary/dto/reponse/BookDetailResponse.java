package com.example.projectlibary.dto.reponse;

import com.example.projectlibary.model.Book;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for {@link Book}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDetailResponse implements Serializable {
    Long id;
     String title;
     String description;
     String isbn;
     String publisher;
     String thumbnail;
     Integer publicationYear;
     BigDecimal replacementCost;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;

    // Dùng DTO của các đối tượng liên quan
     CategoryResponse category;
     Set<AuthorResponse> authors;

    // Thông tin người tạo/cập nhật (có thể dùng DTO đơn giản)
     String createdByUsername;
     String updatedByUsername;

    // Có thể thêm thông tin khác như số lượng bản sao có sẵn, điểm đánh giá trung bình...
     long availableCopies;
     double averageRating;
     long loanCount;
}