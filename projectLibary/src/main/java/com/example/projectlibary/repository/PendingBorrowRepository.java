package com.example.projectlibary.repository;

import com.example.projectlibary.model.PendingBorrow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingBorrowRepository extends JpaRepository<PendingBorrow, Long> {
}