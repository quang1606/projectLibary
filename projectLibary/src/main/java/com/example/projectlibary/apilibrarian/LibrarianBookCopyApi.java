package com.example.projectlibary.apilibrarian;

import com.example.projectlibary.dto.reponse.BookCopyResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.request.CreateBookCopyRequest;
import com.example.projectlibary.dto.request.UpdateBookCopyRequest;
import com.example.projectlibary.service.BookCopyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/book-copies")
@RequiredArgsConstructor
public class LibrarianBookCopyApi {
    private final BookCopyService bookCopyService;
    @GetMapping("/getAll")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<ResponseData<PageResponse<BookCopyResponse>>> getAllBookCopies(@RequestParam Long id,
                                                                                         @RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int size) {
        PageResponse<BookCopyResponse> bookCopy = bookCopyService.getAllBookCopy(id,page,size);
        ResponseData<PageResponse<BookCopyResponse>> responseData = new ResponseData<>(200,"Success",bookCopy);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("/post")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<ResponseData<List<BookCopyResponse>>> createCopies(@Valid @RequestBody CreateBookCopyRequest request){
        List<BookCopyResponse> bookCopyResponse = bookCopyService.createBookCopies(request);
        ResponseData<List<BookCopyResponse>> responseData = new ResponseData<>(200,"Success",bookCopyResponse);
        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/put")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<ResponseData<BookCopyResponse>> updateCopies(@Valid @RequestBody UpdateBookCopyRequest request,@RequestParam Long id){
        BookCopyResponse bookCopyResponse = bookCopyService.updateBookCopies(request,id);
        ResponseData<BookCopyResponse>responseData = new ResponseData<>(200,"Success",bookCopyResponse);
        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<?> deleteCopies(@RequestParam Long copyId){
        bookCopyService.deleteBookCopies(copyId);
        return ResponseEntity.ok().build();
    }

}

