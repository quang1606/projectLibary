package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.BookDetailResponse;
import com.example.projectlibary.dto.reponse.BookSummaryResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateBookRequest;
import com.example.projectlibary.dto.request.UpdateBookRequest;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookElasticSearch;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    PageResponse<BookSummaryResponse> getAllBooks(int page, int size);


    PageResponse<BookSummaryResponse> getMostBorrowedBooks(int page, int size);

    PageResponse<BookSummaryResponse> getNewBooks(int page, int size);

   BookDetailResponse getBookById(long id);

   PageResponse<BookSummaryResponse> getTopRatedBookOfEachAuthor(int page, int size);


    PageResponse<BookElasticSearch> searchBooks(int page, int size, String keyWord);

    PageResponse<BookSummaryResponse> filterBooks(int page, int size, List<String> filter);

    PageResponse<BookSummaryResponse> getBooksByAuthor(Long authorId, int page, int size);

    BookDetailResponse createBook(@Valid CreateBookRequest createBookRequest, MultipartFile pdf,MultipartFile thumbnail);

    BookDetailResponse updateBook(@Valid UpdateBookRequest updateBookRequest, MultipartFile pdf, MultipartFile thumbnail,Long id);

    void deleteBook(Long id);
    void restoreBook(Long id);


//    PageResponse<BookSummaryResponse> getTopRatedBookOfEachAuthor(int page, int size);
}
