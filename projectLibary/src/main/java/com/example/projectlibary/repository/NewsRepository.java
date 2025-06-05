package com.example.projectlibary.repository;

import com.example.projectlibary.model.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
}