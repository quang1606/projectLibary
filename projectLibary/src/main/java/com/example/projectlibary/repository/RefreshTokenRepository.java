package com.example.projectlibary.repository;

import com.example.projectlibary.model.RefreshToken;
import com.example.projectlibary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    void deleteByUser(User user);

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);
}