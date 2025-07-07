package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.AuthorResponse;
import com.example.projectlibary.dto.reponse.PageResponse;

public interface AuthorService {
    PageResponse<AuthorResponse> getAllAuthor(int page, int size);

    AuthorResponse getAuthorById(long id);
}
