package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.PaymentResponse;
import com.example.projectlibary.dto.reponse.PaymentTransactionResponse;
import com.example.projectlibary.dto.request.CreateTransactionRequest;
import com.example.projectlibary.dto.request.VietQRCallbackPayload;

import java.util.List;

public interface PaymentService {
    List<PaymentResponse> getPendingPayment();

    PaymentTransactionResponse createTransaction(CreateTransactionRequest createTransactionRequest);

    void processSuccessfulPayment(VietQRCallbackPayload payload);
}
