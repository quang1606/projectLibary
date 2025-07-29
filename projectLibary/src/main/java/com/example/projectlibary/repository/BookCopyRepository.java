package com.example.projectlibary.repository;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.model.BookCopy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    boolean existsByCopyNumber(String copyNumber);

    boolean existsByQrCode(String potentialQrCode);

    Page<BookCopy> findByBook_Id(Long bookId, Pageable pageable);

    Optional<BookCopy> findByQrCode(String qrCode);

    long countByBook_IdAndStatus(Long bookId, BookCopyStatus bookCopyStatus);
}