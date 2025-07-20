package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAuthorRequest {
    @NotBlank(message = "Author name cannot be blank")
    @Size(max = 255, message = "Author name cannot exceed 255 characters")
    private String name;

    // Tiểu sử là tùy chọn, có thể null
    private String bio;
}
