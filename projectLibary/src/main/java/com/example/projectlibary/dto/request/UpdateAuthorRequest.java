package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAuthorRequest {
    // Không dùng @NotBlank vì đây là tùy chọn
    @Size(max = 255, message = "Author name cannot exceed 255 characters")
    private String name;

    // Tùy chọn
    private String bio;
}
