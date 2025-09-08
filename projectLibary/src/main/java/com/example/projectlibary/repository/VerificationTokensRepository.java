package com.example.projectlibary.repository;

import com.example.projectlibary.model.User;
import com.example.projectlibary.model.VerificationTokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokensRepository extends JpaRepository<VerificationTokens, Long> {
    Optional<VerificationTokens> findByToken(String token);

    Optional<VerificationTokens> findByUser(User user);
}