package com.example.projectlibary.repository;

import com.example.projectlibary.common.BookLoanStatus;
import com.example.projectlibary.model.BookCopy;
import com.example.projectlibary.model.BookLoan;
import com.example.projectlibary.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookLoanRepository extends JpaRepository<BookLoan, Long> {



    Page<BookLoan> findByUser_IdAndStatusIn(Long userId, Collection<BookLoanStatus> statuses, Pageable pageable);

    List<BookLoan> findByStatusAndStudentInitiatedReturnAtBefore(BookLoanStatus status, LocalDateTime studentInitiatedReturnAtBefore);

    Optional<BookLoan> findByBookCopyAndStatus(BookCopy bookCopy, BookLoanStatus status);
}