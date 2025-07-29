package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.BorrowingCartResponse;
import com.example.projectlibary.dto.request.ConfirmLoanRequest;
import com.example.projectlibary.dto.request.VerifyCartItemRequest;
import jakarta.validation.Valid;

public interface LibrarianBorrowingService {
    BorrowingCartResponse getCartForVerificationCode(@Valid ConfirmLoanRequest confirmationCode);

    BorrowingCartResponse verifyCartItem(@Valid VerifyCartItemRequest verifyCartItemRequest, String id);

    void completeLoanSession(Long cartId);
}
