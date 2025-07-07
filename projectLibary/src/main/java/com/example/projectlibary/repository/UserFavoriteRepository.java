package com.example.projectlibary.repository;

import com.example.projectlibary.model.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
}