package com.example.projectlibary.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VietQRCallbackPayload {
    private String transactionIdFromBank;
    private BigDecimal amount;
    private String description;
    private boolean success;
}
