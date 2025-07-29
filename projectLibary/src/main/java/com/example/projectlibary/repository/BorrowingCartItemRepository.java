package com.example.projectlibary.repository;

import com.example.projectlibary.model.BorrowingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowingCartItemRepository extends JpaRepository<BorrowingCartItem, Long> {
    Optional<BorrowingCartItem> findByCart_IdAndBookCopy_Id(Long cartId, Long bookCopyId);
}