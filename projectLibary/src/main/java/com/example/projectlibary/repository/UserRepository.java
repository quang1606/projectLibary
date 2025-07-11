package com.example.projectlibary.repository;

import com.example.projectlibary.common.UserRole;
import com.example.projectlibary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByStudentId(String studentId);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);



    List<User> findByRoleIn(List<UserRole> list);


    Optional<User> findByEmail(String email);
}