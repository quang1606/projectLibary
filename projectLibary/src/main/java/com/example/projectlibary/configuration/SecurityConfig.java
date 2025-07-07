package com.example.projectlibary.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Đảm bảo import đúng
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // DÒNG NÀY RẤT QUAN TRỌNG ĐỂ FIX LỖI 403
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép request GET đến /api/v1/book mà không cần xác thực
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()

                        // (Tùy chọn) Cho phép truy cập Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        // Tất cả các request khác phải được xác thực
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}