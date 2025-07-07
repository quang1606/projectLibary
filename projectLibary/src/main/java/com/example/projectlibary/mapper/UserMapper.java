package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.UserResponse;
import com.example.projectlibary.dto.reponse.UserSummaryResponse;
import com.example.projectlibary.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponse toDetailResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .studentId(user.getStudentId())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .isActive(user.isActive()) // Sử dụng is...() cho kiểu boolean
                .build();
    }


    public UserSummaryResponse toSummaryResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatar()) // Map từ 'avatar' sang 'avatarUrl'
                .build();
    }

    public List<UserSummaryResponse> toSummaryResponseList(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }
}
