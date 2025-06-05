package com.example.projectlibary.repository;

import com.example.projectlibary.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Long> {
}