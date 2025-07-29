package com.example.projectlibary.controller.admin;

import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.reponse.UserResponse;
import com.example.projectlibary.dto.request.UpdateUserRoleRequest;
import com.example.projectlibary.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// controller/AdminUserController.java (hoặc trong UserController)
@RestController
@RequestMapping("/api/admin/users") // Prefix /admin để dễ bảo mật
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Bảo vệ toàn bộ controller
public class AdminUserController {

    private final UserService userService;

    // Cập nhật vai trò
    @PatchMapping("/{userId}/role")
    public ResponseEntity<ResponseData<UserResponse>> updateUserRole(@PathVariable Long userId,
                                                                     @Valid @RequestBody UpdateUserRoleRequest request) {
        UserResponse userResponse = userService.updateUserRole(userId, request.getRole());
        ResponseData<UserResponse> responseData = new ResponseData<>(200,"success",userResponse);
        return ResponseEntity.ok(responseData);

    }
}
