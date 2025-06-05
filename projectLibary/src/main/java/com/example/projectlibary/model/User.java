package com.example.projectlibary.model;

import com.example.projectlibary.common.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "User")
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends AbstractEntity {

        @Column(name = "username", length = 100, nullable = false, unique = true)
        private String username;

        @Column(name = "password", length = 255, nullable = false)
        private String password; // Mật khẩu đã được hash

        @Column(name = "email", length = 255, unique = true)
        private String email;

        @Column(name = "full_name", length = 255)
        private String fullName;

        @Column(name = "student_id", length = 50, unique = true)
        private String studentId; // Mã số sinh viên (nếu là STUDENT)

        @Column(name = "phone_number", length = 20)
        private String phoneNumber;
        @Column(name = "avatar")
        private String avatar;
        @Enumerated(EnumType.STRING) // Lưu trữ giá trị ENUM dưới dạng String trong CSDL
        @Column(name = "role", nullable = false /*, columnDefinition = "ENUM('STUDENT', 'LIBRARIAN', 'ADMIN')"*/)
        // columnDefinition thường không cần thiết nếu DDL được quản lý riêng,
        // nhưng có thể hữu ích nếu Hibernate tạo DDL.
        private UserRole role;

        @Column(name = "is_active", nullable = false /*, columnDefinition = "TINYINT(1) DEFAULT 1"*/)
        // JPA thường map boolean thành TINYINT(1). DEFAULT 1 được xử lý ở CSDL.
        private boolean isActive = true;

    }

