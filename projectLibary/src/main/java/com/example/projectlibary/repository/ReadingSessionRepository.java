package com.example.projectlibary.repository;

import com.example.projectlibary.model.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
}