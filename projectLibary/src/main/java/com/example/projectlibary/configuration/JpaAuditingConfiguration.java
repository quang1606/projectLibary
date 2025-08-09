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
        return () -> {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty(); // Không có người dùng nào đăng nhập
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            return Optional.ofNullable(userDetails.getUser());
        };
    }
}
