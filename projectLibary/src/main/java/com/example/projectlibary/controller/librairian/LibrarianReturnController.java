package com.example.projectlibary.controller.librairian;

import com.example.projectlibary.dto.reponse.BookLoanResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.request.FinalizeReturnRequest;
import com.example.projectlibary.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/librarian/returns")
@PreAuthorize("hasRole('LIBRARIAN')")
@RequiredArgsConstructor
public class LibrarianReturnController {
    private final ReturnService returnService;
    @GetMapping("/find-by-copy/{copyNumber}")
    public ResponseEntity<ResponseData<BookLoanResponse>> getBookLoanByCopy(@PathVariable("copyNumber") long id) {
        BookLoanResponse bookLoanResponse  = returnService.getBookLoanByCopy(id);
        ResponseData<BookLoanResponse> responseData = new ResponseData<>(200,"Success",bookLoanResponse);
        return ResponseEntity.ok(responseData);
    }
    @PostMapping("/finalize")
    public ResponseEntity<ResponseData<BookLoanResponse>> finalizeReturn(@Valid @RequestBody FinalizeReturnRequest request) {
        BookLoanResponse finalizedLoan = returnService.finalizeReturn(request);
        ResponseData<BookLoanResponse> responseData = new ResponseData<>(200,"Success",finalizedLoan);
        return ResponseEntity.ok(responseData);
    }

}
