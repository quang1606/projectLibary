package com.example.projectlibary.controller.student;

import com.example.projectlibary.dto.reponse.*;
import com.example.projectlibary.service.ReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/returns")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class ReturnsController {
    private final ReturnService returnService;
    @GetMapping("my-loans")
    public ResponseEntity<ResponseData<PageResponse<BookLoanResponse>>> getMyLoans(@RequestParam (defaultValue = "0") int page,
                                                                                   @RequestParam (defaultValue = "10") int size) {
        PageResponse<BookLoanResponse> bookSummaryResponsePageResponse = returnService.getMyLoans(page,size);
        ResponseData<PageResponse<BookLoanResponse>> responseData = new ResponseData<>(200,"Success",bookSummaryResponsePageResponse);
        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/{loanId}/request-return")
    public ResponseEntity<ResponseData<BookLoanResponse>> returnsBook(@PathVariable Long loanId){
        BookLoanResponse bookLoanResponse = returnService.returnsBook(loanId);
        ResponseData<BookLoanResponse> responseData = new ResponseData<>(200,"Success",bookLoanResponse);
        return ResponseEntity.ok(responseData);
    }
    @PostMapping("/lost/{loanId}")
    public ResponseEntity<ResponseData<BookLoanResponse>> lostBook(@PathVariable Long loanId){
        BookLoanResponse bookLoanResponse = returnService.lostBook(loanId);
        ResponseData<BookLoanResponse> responseData = new ResponseData<>(200,"Success",bookLoanResponse);
        return ResponseEntity.ok(responseData);
    }


}
