package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.BookReviewResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateReviewRequest;
import com.example.projectlibary.dto.request.UpdateReviewRequest;
import jakarta.validation.Valid;

public interface BookReviewService {
    PageResponse<BookReviewResponse> getAllReview(int page, int size, Long id);

    BookReviewResponse createReview(@Valid CreateReviewRequest createReviewRequest);

    BookReviewResponse updateReview(@Valid UpdateReviewRequest updateReviewRequest,Long id);

    void deleteReview(Long id);
}
