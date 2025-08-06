package com.example.projectlibary.dto.reponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
public class PaymentTransactionResponse {
    private String transactionId;
    private BigDecimal amount;
    private String vietQrCode;
    private String description;
    private LocalDateTime expirationDate;
}
