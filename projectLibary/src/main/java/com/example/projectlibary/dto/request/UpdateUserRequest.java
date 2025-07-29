package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    // Người dùng có thể muốn cập nhật tên đầy đủ của họ
    @Size(max = 255, message = "Full name cannot exceed 255 characters")
    private String fullName;

    // Người dùng có thể muốn cập nhật mã số sinh viên
    @Size(max = 50, message = "Student ID cannot exceed 50 characters")
    private String studentId;

    // Người dùng có thể muốn cập nhật số điện thoại
    @Pattern(regexp = "^(\\+84|0)\\d{9,10}$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

}
