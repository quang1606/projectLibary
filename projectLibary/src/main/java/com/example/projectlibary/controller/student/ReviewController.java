package com.example.projectlibary.controller.student;

import com.example.projectlibary.dto.reponse.BookReviewResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.request.CreateReviewRequest;
import com.example.projectlibary.dto.request.UpdateReviewRequest;
import com.example.projectlibary.service.BookReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {
    private final BookReviewService bookReviewService;
    @GetMapping("/get")

    public ResponseEntity<ResponseData<PageResponse<BookReviewResponse>>> getReview(@RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue ="10") int size ,
                                                                                    @PathVariable Long id) {
        PageResponse<BookReviewResponse> bookReviewResponse = bookReviewService.getAllReview(page,size,id);
        ResponseData<PageResponse<BookReviewResponse>> reviews = new ResponseData<>(200, "Success", bookReviewResponse);
        return ResponseEntity.ok(reviews);

    }
    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResponseData<BookReviewResponse>> CreateReview(@RequestBody @Valid CreateReviewRequest createReviewRequest){
        BookReviewResponse bookReview = bookReviewService.createReview(createReviewRequest);
        ResponseData<BookReviewResponse> reviews = new ResponseData<>(200, "Success", bookReview);
        return ResponseEntity.ok(reviews);
    }
   @PutMapping("/update/{id}")
   @PreAuthorize("hasRole('STUDENT')")
   public ResponseEntity<ResponseData<BookReviewResponse>> updateReview(@RequestBody @Valid UpdateReviewRequest updateReviewRequest, @PathVariable Long id){
       BookReviewResponse bookReview = bookReviewService.updateReview(updateReviewRequest,id);
       ResponseData<BookReviewResponse> reviews = new ResponseData<>(200, "Success", bookReview);
       return ResponseEntity.ok(reviews);
   }
   @DeleteMapping("/delete/{id}")
   @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id){
        bookReviewService.deleteReview(id);
        return ResponseEntity.ok().build();
   }
}
