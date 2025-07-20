package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
public class CreateReviewRequest {
    @NotNull(message = "Book Id cannot null")
    private Long bookId;
    @NotNull(message = "Rating is required")
    @Min(value = 1,message = "Rating must be at least 1")
    @Max(value = 5,message = "Rating must be at most 5 ")
    private Integer rating;
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    private String comment; // Bình luận có thể là tùy chọn (nullable)
}
