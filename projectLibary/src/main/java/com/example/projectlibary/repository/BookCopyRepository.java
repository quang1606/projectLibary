package com.example.projectlibary.repository;

import com.example.projectlibary.model.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    boolean existsByCopyNumber(String copyNumber);
}