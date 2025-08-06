package com.example.projectlibary.controller;

import com.example.projectlibary.dto.request.VietQRCallbackPayload;
import com.example.projectlibary.event.PaymentCompletedEvent;
import com.example.projectlibary.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {
    private final PaymentService paymentService;

    @PostMapping("/vietqr-callback")
    public ResponseEntity<String> callback(@RequestBody VietQRCallbackPayload payload) {
        paymentService.processSuccessfulPayment(payload);
        return ResponseEntity.ok("success");
    }
}
