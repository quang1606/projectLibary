package com.example.projectlibary.repository;

import com.example.projectlibary.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    boolean existsByName(String authorName);
}