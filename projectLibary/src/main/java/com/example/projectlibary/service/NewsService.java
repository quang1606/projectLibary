package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.AuthorResponse;
import com.example.projectlibary.dto.reponse.NewsDetailResponse;
import com.example.projectlibary.dto.reponse.NewsSummaryResponse;
import com.example.projectlibary.dto.reponse.PageResponse;

public interface NewsService {

    PageResponse<NewsSummaryResponse> getAllNew(int page, int size);

    NewsDetailResponse getNewById(long id);
}
