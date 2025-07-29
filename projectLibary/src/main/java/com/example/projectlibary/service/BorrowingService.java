package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.BorrowingCartItemResponse;
import com.example.projectlibary.dto.reponse.BorrowingCartResponse;
import com.example.projectlibary.dto.request.AddItemToCartRequest;
import com.example.projectlibary.dto.request.ConfirmLoanRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface BorrowingService {
    BorrowingCartResponse addItemToCart(@Valid AddItemToCartRequest request);

    BorrowingCartResponse getAllCartItems();

    BorrowingCartResponse generateConfirmation();

    BorrowingCartResponse removeItemFromCart(Long id);
}
