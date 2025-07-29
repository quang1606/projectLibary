package com.example.projectlibary.repository;

import com.example.projectlibary.common.BorrowingCartStatus;
import com.example.projectlibary.model.BorrowingCart;
import com.example.projectlibary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowingCartRepository extends JpaRepository<BorrowingCart, Long> {
    Optional<BorrowingCart> findByUser_IdAndStatus(Long userId, BorrowingCartStatus status);

    Optional<BorrowingCart> findByConfirmationCode(String confirmationCode);

    boolean existsByConfirmationCode(String confirmationCode);

}