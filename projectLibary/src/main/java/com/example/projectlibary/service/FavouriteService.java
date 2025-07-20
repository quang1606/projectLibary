package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.FavouriteBookResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.ToggleFavoriteResponse;

public interface FavouriteService {

    PageResponse<FavouriteBookResponse> getAllFavourite(int page, int size);


    void deleteFavouriteAllBook();

    boolean isFavourite(Long id);

    ToggleFavoriteResponse toggleFavoriteBook(Long bookId);
}
