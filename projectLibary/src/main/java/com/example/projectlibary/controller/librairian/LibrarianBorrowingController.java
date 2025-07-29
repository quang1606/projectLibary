package com.example.projectlibary.controller.librairian;

import com.example.projectlibary.dto.reponse.BorrowingCartResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.request.ConfirmLoanRequest;
import com.example.projectlibary.dto.request.VerifyCartItemRequest;
import com.example.projectlibary.service.LibrarianBorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/librarian/borrowing")
@PreAuthorize("hasRole('LIBRARIAN')")
public class LibrarianBorrowingController {
    private final LibrarianBorrowingService librarianBorrowingService;
    @GetMapping("/cart")
    public ResponseEntity<ResponseData<BorrowingCartResponse>> getCartForVerificationCode (@Valid @RequestBody ConfirmLoanRequest confirmationCode) {
        BorrowingCartResponse borrowingCartResponse = librarianBorrowingService.getCartForVerificationCode(confirmationCode);
        ResponseData<BorrowingCartResponse> responseData = new ResponseData<>(200,"Success",borrowingCartResponse);
        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/cart/{id}/verify-item")
    public ResponseEntity<ResponseData<BorrowingCartResponse>> verifyCartItem(@Valid @RequestBody VerifyCartItemRequest verifyCartItemRequest, @PathVariable String id){
        BorrowingCartResponse borrowingCartResponse = librarianBorrowingService.verifyCartItem(verifyCartItemRequest,id);
        ResponseData<BorrowingCartResponse> responseData = new ResponseData<>(200,"Success",borrowingCartResponse);
        return ResponseEntity.ok(responseData);

    }
    @PostMapping("/cart/{cartId}/complete-loan")
    public ResponseEntity<ResponseData<String>> completeLoanSession(@PathVariable Long cartId){
        librarianBorrowingService.completeLoanSession(cartId);
        ResponseData<String> responseData = new ResponseData<>(200,"Loan session completed successfully",null);
        return ResponseEntity.ok(responseData);
    }




}
