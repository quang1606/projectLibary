package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
public class UpdateBookRequest {
    @NotBlank(message = "Title not blank")
    @Size(max = 255,message = "Title cannot exceed 255 characters")
    private String title;
    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotBlank(message = "ISBN cannot be blank")
    @Size(max = 20, message = "ISBN cannot exceed 20 characters")
    private String isbn;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId; // Chỉ cần nhận ID của thể loại

    @Size(max = 255, message = "Publisher cannot exceed 255 characters")
    private String publisher;

    @NotNull(message = "Publication year cannot be null")
    @Min(value = 1000, message = "Publication year must be a valid year")
    @Max(value = 9999, message = "Publication year must be a valid year")
    private Integer publicationYear;

    // (Tùy chọn) URL đến ảnh bìa, có thể là null
    private String thumbnail;

    // (Tùy chọn) URL đến ebook, có thể là null
    @Size(max = 512, message = "Ebook URL cannot exceed 512 characters")
    private String ebookUrl;

    @NotNull(message = "Replacement cost cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Replacement cost must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid format for replacement cost")
    private BigDecimal replacementCost;

    @NotEmpty(message = "A book must have at least one author")
    private Set<Long> authorIds; // Chỉ cần nhận một danh sách các ID của tác giả
}

