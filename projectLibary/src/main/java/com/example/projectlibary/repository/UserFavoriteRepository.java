package com.example.projectlibary.repository;

import com.example.projectlibary.model.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    Page<UserFavorite> findByUser_Id(Long userId, Pageable pageable);


    int deleteByBook_IdAndUser_Id(Long bookId, Long userId);

    void deleteByUser_Id(Long id);

    boolean existsByUser_IdAndBook_Id(Long id, Long id1);

    Optional<UserFavorite> findByBook_IdAndUser_Id(Long bookId, Long id);
}