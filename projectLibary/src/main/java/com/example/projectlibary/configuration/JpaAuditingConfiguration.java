package com.example.projectlibary.configuration;

import com.example.projectlibary.model.CustomUserDetails;
import com.example.projectlibary.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class JpaAuditingConfiguration {
    @Bean
    public AuditorAware<User> auditorProvider() {
        // Sử dụng biểu thức lambda để code ngắn gọn
        return () -> {
            // Lấy thông tin xác thực từ SecurityContext của Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Kiểm tra xem người dùng đã đăng nhập hay chưa
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty(); // Không có người dùng nào đăng nhập
            }

            // Lấy đối tượng CustomUserDetails từ Principal
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Trả về Optional chứa đối tượng User
            return Optional.ofNullable(userDetails.getUser());
        };
    }
}
