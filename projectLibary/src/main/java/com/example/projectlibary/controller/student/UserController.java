package com.example.projectlibary.controller.student;

import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.reponse.UserResponse;
import com.example.projectlibary.dto.request.UpdateUserRequest;
import com.example.projectlibary.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/user")

public class UserController {
    private final UserService userService;
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') and hasRole('LIBRAIRIAN')")
    public ResponseEntity<ResponseData<UserResponse>> updateUser(@Valid @RequestPart UpdateUserRequest updateUserRequest,
                                                                 @RequestPart MultipartFile multipartFile) {
        UserResponse userResponse = userService.updateUser(updateUserRequest,multipartFile);
        ResponseData<UserResponse> responseData = new ResponseData<>(200,"success",userResponse);
        return  ResponseEntity.ok(responseData);
    }

}
