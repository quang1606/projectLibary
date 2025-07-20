package com.example.projectlibary.dto.request;

import com.example.projectlibary.common.NewsStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNewsRequest {
    // Không dùng @NotBlank vì người dùng có thể không muốn cập nhật tiêu đề
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    // Người dùng có thể chỉ muốn thay đổi tiêu đề hoặc trạng thái mà không chạm vào nội dung
    private String content; // Vẫn sẽ chứa mã HTML

    // Người dùng có thể chỉ muốn sửa nội dung mà không thay đổi trạng thái
    private NewsStatus status;
}
