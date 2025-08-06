package com.example.projectlibary.controller.student;

import com.example.projectlibary.dto.reponse.PaymentResponse;
import com.example.projectlibary.dto.reponse.PaymentTransactionResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.request.CreateTransactionRequest;
import com.example.projectlibary.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/payment")
@PreAuthorize("hasRole('STUDENT')")
public class PaymentController {
    private final PaymentService paymentService;
    @GetMapping("/me/pending")
    public ResponseEntity<ResponseData<List<PaymentResponse>>> getPendingPayments() {
        List<PaymentResponse> paymentResponse = paymentService.getPendingPayment();
        ResponseData<List<PaymentResponse>> responseData = new ResponseData<>(200,"ok",paymentResponse) ;
        return ResponseEntity.ok(responseData);
    }
    @PostMapping("/me/create-transaction")
    public ResponseEntity<ResponseData<PaymentTransactionResponse>> createTransaction(@Valid @RequestBody CreateTransactionRequest createTransactionRequest) {
        PaymentTransactionResponse paymentTransactionResponse = paymentService.createTransaction(createTransactionRequest);
        ResponseData<PaymentTransactionResponse> responseData = new ResponseData<>(200,"ok",paymentTransactionResponse);
        return ResponseEntity.ok(responseData);
    }

}
