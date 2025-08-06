package com.example.projectlibary.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private Long userId;
    private String transactionId;
    private BigDecimal amountPaid;
    private LocalDateTime paymentDate;
}
