package com.example.projectlibary.dto.request;

import com.example.projectlibary.common.BookCopyStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookCopyRequest {
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location; // Cho phép null nếu không muốn thay đổi

    private BookCopyStatus status; // Cho phép null nếu không muốn thay đổi

    // Có thể thêm các trường khác nếu cần, ví dụ: copyNumber
}
