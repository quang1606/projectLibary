package com.example.projectlibary.apistudent;

import com.example.projectlibary.dto.reponse.*;
import com.example.projectlibary.model.BookElasticSearch;
import com.example.projectlibary.service.AuthorService;
import com.example.projectlibary.service.BookService;
import com.example.projectlibary.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class BookController {
    private final BookService bookService;
    private final AuthorService authorService;
    private final NewsService newsService;
    @GetMapping("") // Đã sửa
    public ResponseEntity<ResponseData<PageResponse<BookSummaryResponse>>> getMostBorrowedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<BookSummaryResponse> mostBorrowedBooks = bookService.getMostBorrowedBooks(page, size);
        ResponseData<PageResponse<BookSummaryResponse>> responseData =
                new ResponseData<>(200, "Success", mostBorrowedBooks);
        return ResponseEntity.ok(responseData);
    }
    @GetMapping("/book/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseData<PageResponse<BookSummaryResponse>>> getAllBooks( @RequestParam(defaultValue = "0") int page,
                                                                                        @RequestParam(defaultValue = "10") int size){
        PageResponse<BookSummaryResponse> allbook=bookService.getAllBooks(page,size);
        ResponseData<PageResponse<BookSummaryResponse>> responseData =new ResponseData<>(200, "Success", allbook);
        return ResponseEntity.ok(responseData);
    }
    @GetMapping("/book/new-releases")
    public ResponseEntity<ResponseData<PageResponse<BookSummaryResponse>>> getNewBooks(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "12") int size){
        PageResponse<BookSummaryResponse> newBooks = bookService.getNewBooks(page,size);
        ResponseData<PageResponse<BookSummaryResponse>> newBooksResponse = new ResponseData<>(200, "Success", newBooks);
        return ResponseEntity.ok(newBooksResponse);

    }
    @GetMapping("/book/{id}")
    public ResponseEntity<ResponseData<BookDetailResponse>> getBookById(@PathVariable("id") long id){
       BookDetailResponse bookDetail = bookService.getBookById(id);
       ResponseData<BookDetailResponse> responseData = new ResponseData<>(200, "Success", bookDetail);
       return ResponseEntity.ok(responseData);

    }

    @GetMapping("/book/best-books")
//   @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResponseData<PageResponse<BookSummaryResponse>>> getTopRatedBooksOfEachAuthor(@RequestParam(defaultValue = "0") int page,
                                                                                                        @RequestParam(defaultValue = "12") int size){
        PageResponse<BookSummaryResponse> bestBooks= bookService.getTopRatedBookOfEachAuthor(page,size);
        ResponseData<PageResponse<BookSummaryResponse>> responseData =new ResponseData<>(200,"Success", bestBooks);
        return ResponseEntity.ok(responseData);
    }


    @GetMapping("/search-es")
    public  ResponseEntity<ResponseData<PageResponse<BookElasticSearch>>> getBooksWithElasticSearch(@RequestParam(defaultValue = "0") int page,
                                                                                                      @RequestParam(defaultValue = "12") int size,
                                                                                                      @RequestParam(required = false) String keyword){
        PageResponse<BookElasticSearch> searchBooks =  bookService.searchBooks(page,size,keyword);
        ResponseData<PageResponse<BookElasticSearch>> responseData =new ResponseData<>(200,"Success", searchBooks);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/book/filter")
    public ResponseEntity<ResponseData<PageResponse<BookSummaryResponse>>> SearchFilter (@RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "12") int size,
                                                                           @RequestParam (required = false) List<String> filter){
        PageResponse<BookSummaryResponse> searchFilter = bookService.filterBooks(page,size,filter);
        ResponseData<PageResponse<BookSummaryResponse>> responseData =new ResponseData<>(200,"Success", searchFilter);
        return ResponseEntity.ok(responseData);
    }
    @GetMapping("/author")
    public ResponseEntity<ResponseData<PageResponse<AuthorResponse>>> getAllAuthor(@RequestParam (defaultValue = "0") int page,
                                                                                   @RequestParam(defaultValue = "18") int size){
        PageResponse<AuthorResponse> pageResponse = authorService.getAllAuthor(page,size);
        ResponseData<PageResponse<AuthorResponse>> responseData = new ResponseData<>(200,"Success",pageResponse);
        return ResponseEntity.ok(responseData);
    }
    @GetMapping("/author/{id}")
    public ResponseEntity<ResponseData<AuthorResponse>> getAuthorById(@PathVariable("id") long id){
        AuthorResponse authorById = authorService.getAuthorById(id);
        ResponseData<AuthorResponse> responseData = new ResponseData<>(200,"Success",authorById);
        return ResponseEntity.ok(responseData);
    }
    @GetMapping("/{authorId}/books")
    public ResponseEntity<ResponseData<PageResponse<BookSummaryResponse>>> getBookByAuthor( @PathVariable Long authorId,
                                                                                            @RequestParam(defaultValue = "0") int page,
                                                                                            @RequestParam(defaultValue = "12") int size){
        PageResponse<BookSummaryResponse> pageResponse = bookService.getBooksByAuthor(authorId,page,size);
        ResponseData<PageResponse<BookSummaryResponse>> responseData =new ResponseData<>(200,"Success",pageResponse);
        return ResponseEntity.ok(responseData);
    }


}
