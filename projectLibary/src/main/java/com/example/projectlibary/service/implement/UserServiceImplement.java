package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.UserRole;
import com.example.projectlibary.dto.reponse.UserResponse;
import com.example.projectlibary.dto.request.UpdateUserRequest;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.UserMapper;
import com.example.projectlibary.model.User;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.service.CloudinaryService;
import com.example.projectlibary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImplement implements UserService {
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final UserMapper userMapper;
    @Override
    public UserResponse updateUser(UpdateUserRequest updateUserRequest, MultipartFile multipartFile) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));
        if(updateUserRequest.getFullName()!=null && !updateUserRequest.getFullName().isEmpty()) {
            user.setFullName(updateUserRequest.getFullName());
        }
        if(updateUserRequest.getPhoneNumber()!=null && !updateUserRequest.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(updateUserRequest.getPhoneNumber());
        }
        if (updateUserRequest.getStudentId()!=null && !updateUserRequest.getStudentId().isEmpty()) {
            user.setStudentId(updateUserRequest.getStudentId());
        }
        if (multipartFile!=null && !multipartFile.isEmpty()) {
            try {
                cloudinaryService.uploadFile(multipartFile,"library/avatarUser");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        User updatedUser = userRepository.save(user);
        return userMapper.toDetailResponse(updatedUser);
    }

    @Override
    public UserResponse updateUserRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));
        user.setRole(role);
        userRepository.save(user);
        return userMapper.toDetailResponse(user);
    }
}
