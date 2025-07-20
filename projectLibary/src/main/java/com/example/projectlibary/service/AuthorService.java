package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.AuthorResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateAuthorRequest;
import com.example.projectlibary.dto.request.UpdateAuthorRequest;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface AuthorService {
    PageResponse<AuthorResponse> getAllAuthor(int page, int size);

    AuthorResponse getAuthorById(long id);

    AuthorResponse createAuthor(@Valid CreateAuthorRequest request, MultipartFile avatarFile);

    AuthorResponse updateAuthor(@Valid UpdateAuthorRequest request, MultipartFile avatarFile, Long id);
}
