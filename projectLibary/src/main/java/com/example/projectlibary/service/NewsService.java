package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.AuthorResponse;
import com.example.projectlibary.dto.reponse.NewsDetailResponse;
import com.example.projectlibary.dto.reponse.NewsSummaryResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateNewsRequest;
import com.example.projectlibary.dto.request.UpdateNewsRequest;
import jakarta.validation.Valid;

public interface NewsService {

    PageResponse<NewsSummaryResponse> getAllNew(int page, int size);

    NewsDetailResponse getNewById(long id);

    NewsDetailResponse createNews(@Valid CreateNewsRequest createNewsRequest);

    NewsDetailResponse updateNews(@Valid UpdateNewsRequest updateNewsRequest, long id);

    void deletePost(long id);
}
