package com.example.projectlibary.repository;

import com.example.projectlibary.model.BookLoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {
}