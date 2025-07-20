package com.example.projectlibary.dto.request;

import com.example.projectlibary.common.NewsStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNewsRequest {
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    private String content; // Sẽ chứa mã HTML

    @NotNull(message = "Status cannot be null")
    private NewsStatus status; // Trạng thái của bài viết (DRAFT, PUBLISHED)
}

