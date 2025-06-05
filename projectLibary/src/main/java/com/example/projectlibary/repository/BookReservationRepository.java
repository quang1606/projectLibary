package com.example.projectlibary.repository;

import com.example.projectlibary.model.BookReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookReservationRepository extends JpaRepository<BookReservation, Long> {
}