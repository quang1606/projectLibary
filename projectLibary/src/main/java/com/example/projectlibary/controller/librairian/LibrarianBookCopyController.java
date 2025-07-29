package com.example.projectlibary.controller.librairian;

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
@PreAuthorize("hasRole('LIBRARIAN')")

@RequiredArgsConstructor
public class LibrarianBookCopyController {
    private final BookCopyService bookCopyService;
    @GetMapping("/getAll")

    public ResponseEntity<ResponseData<PageResponse<BookCopyResponse>>> getAllBookCopies(@RequestParam Long id,
                                                                                         @RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int size) {
        PageResponse<BookCopyResponse> bookCopy = bookCopyService.getAllBookCopy(id,page,size);
        ResponseData<PageResponse<BookCopyResponse>> responseData = new ResponseData<>(200,"Success",bookCopy);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("/post")
    public ResponseEntity<ResponseData<List<BookCopyResponse>>> createCopies(@Valid @RequestBody CreateBookCopyRequest request){
        List<BookCopyResponse> bookCopyResponse = bookCopyService.createBookCopies(request);
        ResponseData<List<BookCopyResponse>> responseData = new ResponseData<>(200,"Success",bookCopyResponse);
        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/put")
    public ResponseEntity<ResponseData<BookCopyResponse>> updateCopies(@Valid @RequestBody UpdateBookCopyRequest request,@RequestParam Long id){
        BookCopyResponse bookCopyResponse = bookCopyService.updateBookCopies(request,id);
        ResponseData<BookCopyResponse>responseData = new ResponseData<>(200,"Success",bookCopyResponse);
        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteCopies(@RequestParam Long copyId){
        bookCopyService.deleteBookCopies(copyId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{copyId}/qr-code-image")
    public ResponseEntity<ResponseData<String>> getQRCodeImage(@PathVariable Long copyId){
        String qrcode= bookCopyService.getQRCodeImage(copyId);
        ResponseData<String> responseData = new ResponseData<>(200,"Success",qrcode);
        return ResponseEntity.ok(responseData);
    }


}

