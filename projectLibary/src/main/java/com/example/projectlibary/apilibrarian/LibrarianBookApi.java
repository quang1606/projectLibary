package com.example.projectlibary.apilibrarian;

import com.example.projectlibary.dto.reponse.BookDetailResponse;
import com.example.projectlibary.dto.reponse.BookSummaryResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.request.CreateBookRequest;
import com.example.projectlibary.dto.request.UpdateBookRequest;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/book")
@RequiredArgsConstructor

public class LibrarianBookApi {
    private final BookService bookService;
    @GetMapping("")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<ResponseData<PageResponse<BookSummaryResponse>>> getAllBooks(@RequestParam (defaultValue = "0") int page,
                                                                                       @RequestParam (defaultValue = "10") int size) {
        PageResponse<BookSummaryResponse> pageResponse = bookService.getAllBooks(page, size);
        ResponseData<PageResponse<BookSummaryResponse>> responseData  = new ResponseData<>(200,"success",pageResponse);
        return ResponseEntity.ok(responseData);
    }
    @PostMapping("/post")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<ResponseData<BookDetailResponse>> createBook(@Valid @RequestPart CreateBookRequest createBookRequest,
                                                                       @RequestPart(required = true) MultipartFile pdf,
                                                                       @RequestPart(required = true) MultipartFile thumbnail) {
        BookDetailResponse bookDetailResponse = bookService.createBook(createBookRequest, pdf,thumbnail);
        ResponseData<BookDetailResponse> responseData = new ResponseData<>(200,"success",bookDetailResponse);
        return ResponseEntity.ok(responseData);
    }
    @PutMapping("/pust/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<ResponseData<BookDetailResponse>> updateBook(@Valid @RequestPart UpdateBookRequest updateBookRequest,
                                                                       @RequestPart(required = true) MultipartFile pdf,
                                                                       @RequestPart(required = true) MultipartFile thumbnail,
                                                                       @RequestPart  Long id){
        BookDetailResponse bookDetailResponse = bookService.updateBook(updateBookRequest, pdf,thumbnail,id);
        ResponseData<BookDetailResponse> responseData = new ResponseData<>(200,"success",bookDetailResponse);
        return ResponseEntity.ok(responseData);
    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<?> deleteBook(@PathVariable Long id){
        bookService.deleteBook(id);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/{id}/restore") // Dùng POST cho hành động thay đổi trạng thái
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> restoreBook(@PathVariable Long id) {
        bookService.restoreBook(id);
        return ResponseEntity.ok("Book with ID " + id + " has been successfully restored.");
    }


}
