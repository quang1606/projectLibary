package com.example.projectlibary.service;

import com.example.projectlibary.common.UserRole;
import com.example.projectlibary.dto.reponse.UserResponse;
import com.example.projectlibary.dto.request.UpdateUserRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public interface  UserService {
    UserResponse updateUser(@Valid UpdateUserRequest updateUserRequest, MultipartFile multipartFile);

    UserResponse updateUserRole(Long userId, @NotNull UserRole role);
}
