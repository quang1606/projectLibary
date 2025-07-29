package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.BookCopyResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateBookCopyRequest;
import com.example.projectlibary.dto.request.UpdateBookCopyRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface BookCopyService {


    List<BookCopyResponse> createBookCopies(@Valid CreateBookCopyRequest request);

    PageResponse<BookCopyResponse> getAllBookCopy(Long id, int page, int size);

    BookCopyResponse updateBookCopies(@Valid UpdateBookCopyRequest request, Long id);

    void deleteBookCopies(Long id);

    String getQRCodeImage(Long copyId);
}
