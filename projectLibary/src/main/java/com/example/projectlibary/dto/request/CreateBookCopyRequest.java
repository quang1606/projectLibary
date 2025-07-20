package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBookCopyRequest {
    @NotNull(message = "Book ID cannot be null")
    private Long bookId; // ID của đầu sách (Book) mà bản sao này thuộc về

    @NotBlank(message = "Location cannot be blank")
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location; // Vị trí của các bản sao này trên kệ (ví dụ: "Kệ A-01")

    @NotNull(message = "Number of copies cannot be null")
    @Min(value = 1, message = "Must add at least 1 copy")
    private Integer numberOfCopies; // Số lượng bản sao cần tạo trong một lần
}
