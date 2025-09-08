package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.PaymentStatus;
import com.example.projectlibary.common.ReturnCondition;
import com.example.projectlibary.event.FineIssuedEvent;
import com.example.projectlibary.model.BookLoan;
import com.example.projectlibary.model.Payment;
import com.example.projectlibary.repository.PaymentRepository;
import com.example.projectlibary.service.FineService;
import com.example.projectlibary.service.eventservice.KafkaProducerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class FineServiceImplement implements FineService {
private final PaymentRepository paymentRepository;
private final KafkaProducerService kafkaProducerService;
    @Override
    @Transactional
    public void createFineForLoan(BookLoan loan, ReturnCondition condition, BigDecimal replacementCost) {
        BigDecimal fineAmount = calculateFineAmount(condition,replacementCost);
        if (fineAmount != null && fineAmount.compareTo(BigDecimal.ZERO) > 0) {
            Payment newPayment = Payment.builder()
                    .user(loan.getUser())
                    .loan(loan)
                    .amount(fineAmount)
                    .status(PaymentStatus.PENDING)
                    .note("Phạt trả lại sách kèm theo điều kiện: " + condition.name())
                    // paymentDate sẽ được gán khi thanh toán
                    .build();
           Payment savedPayment= paymentRepository.save(newPayment);
            FineIssuedEvent event = FineIssuedEvent.builder()
                    .userId(loan.getUser().getId())
                    .userEmail(loan.getUser().getEmail())
                    .userFullName(loan.getUser().getFullName())
                    .loanId(loan.getId())
                    .bookTitle(loan.getBookCopy().getBook().getTitle())
                    .bookCopyNumber(loan.getBookCopy().getCopyNumber())
                    .paymentId(savedPayment.getId())
                    .fineAmount(savedPayment.getAmount())
                    .reason(condition)
                    .librarianNotes(loan.getReturnNotes())
                    .eventTimestamp(LocalDateTime.now())
                    .build();

            kafkaProducerService.sendFineIssuedEvent(event);
        }
        loan.setFineAmount(fineAmount);

    }
    private BigDecimal calculateFineAmount(ReturnCondition condition, BigDecimal replacementCost) {
        switch (condition) {
            case SLIGHTLY_DAMAGED:
                return replacementCost.multiply(new BigDecimal("0.25")); // Phạt 25%
            case HEAVILY_DAMAGED:
                return replacementCost; // Phạt 50%
            case LOST:
                return replacementCost; // Phạt 100%
            case NORMAL:
            default:
                return BigDecimal.ZERO; // Không phạt
        }
    }
}
