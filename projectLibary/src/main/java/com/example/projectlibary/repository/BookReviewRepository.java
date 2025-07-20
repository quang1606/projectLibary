package com.example.projectlibary.repository;

import com.example.projectlibary.dto.reponse.BookReviewResponse;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookReview;
import com.example.projectlibary.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    boolean existsByUserAndBook(User user, Book book);


    Page<BookReview> findByBookId(Long id, Pageable pageable);
}