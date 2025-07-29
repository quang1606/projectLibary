package com.example.projectlibary.controller.student;

import com.example.projectlibary.dto.reponse.BorrowingCartItemResponse;
import com.example.projectlibary.dto.reponse.BorrowingCartResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.request.AddItemToCartRequest;
import com.example.projectlibary.dto.request.ConfirmLoanRequest;
import com.example.projectlibary.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class BorrowingController {
    private final BorrowingService borrowingService;
    @PostMapping("/items")
    public ResponseEntity<ResponseData<BorrowingCartResponse>> addItemToCart(@Valid @RequestBody AddItemToCartRequest request) {
        BorrowingCartResponse borrowingCartResponse = borrowingService.addItemToCart(request);
        ResponseData<BorrowingCartResponse> responseData = new ResponseData<>(200,"Success", borrowingCartResponse);
        return  ResponseEntity.ok(responseData);
    }
    @GetMapping("")
    public ResponseEntity<ResponseData<BorrowingCartResponse>> getAllCartItems() {
        BorrowingCartResponse borrowingCartResponse = borrowingService.getAllCartItems();
        ResponseData<BorrowingCartResponse> responseData = new ResponseData<>(200,"Success", borrowingCartResponse);
        return  ResponseEntity.ok(responseData);
    }
    @PostMapping("/generate-confirmation")
    public ResponseEntity<ResponseData<BorrowingCartResponse>> generateConfirmation() {
        BorrowingCartResponse borrowingCartResponse = borrowingService.generateConfirmation();
        ResponseData<BorrowingCartResponse> responseData = new ResponseData<>(200,"Success", borrowingCartResponse);
        return  ResponseEntity.ok(responseData);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<BorrowingCartResponse>> removeItemFromCart(@PathVariable Long id) {
        BorrowingCartResponse borrowingCartResponse = borrowingService.removeItemFromCart(id);
        ResponseData<BorrowingCartResponse> responseData = new ResponseData<>(200,"Success", borrowingCartResponse);
        return  ResponseEntity.ok(responseData);
    }


}
