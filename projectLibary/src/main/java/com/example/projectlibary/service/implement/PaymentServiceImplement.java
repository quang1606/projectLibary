package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.PaymentStatus;
import com.example.projectlibary.dto.reponse.PaymentResponse;
import com.example.projectlibary.dto.reponse.PaymentTransactionResponse;
import com.example.projectlibary.dto.request.CreateTransactionRequest;
import com.example.projectlibary.dto.request.VietQRCallbackPayload;
import com.example.projectlibary.event.PaymentCompletedEvent;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.PaymentMapper;
import com.example.projectlibary.model.CustomUserDetails;
import com.example.projectlibary.model.Payment;
import com.example.projectlibary.model.User;
import com.example.projectlibary.repository.PaymentRepository;
import com.example.projectlibary.service.PaymentService;
import com.example.projectlibary.service.eventservice.KafkaProducerService;
import com.example.projectlibary.utils.VietQRGeneratorUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImplement implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final VietQRGeneratorUtil vietQRGeneratorUtil;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public List<PaymentResponse> getPendingPayment() {
        Long userId = getUserId();
        List<Payment> payments = paymentRepository.findByUser_IdAndStatus(userId, PaymentStatus.PENDING);
        return paymentMapper.toResponseList(payments);
    }

    @Override
    @Transactional
    public PaymentTransactionResponse createTransaction(CreateTransactionRequest createTransactionRequest) {
         Long UserId = getUserId();
        List<Payment> payments = paymentRepository.findByIdInAndUser_IdAndStatus(createTransactionRequest.getTransactionIds(), UserId, PaymentStatus.PENDING);
        if(payments.size()!=createTransactionRequest.getTransactionIds().size()){
            throw new AppException(ErrorCode.INVALID_PAYMENT_REQUEST);
        }
        BigDecimal totalAmount = payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        String transactionId = "TXN"+ UUID.randomUUID().toString().toUpperCase().substring(0,8);
        for (Payment payment : payments) {
            payment.setTransactionId(transactionId);
        }
        String bankBin = "970423";
        String bankAccountNumber = "07600586666";
        String accountName = "Nguyen Xuan Quang";
        String description = "TT " + transactionId;
        String  vietQr = vietQRGeneratorUtil.generate(bankBin,bankAccountNumber,accountName,totalAmount,description);
        return PaymentTransactionResponse.builder()
                .transactionId(transactionId)
                .amount(totalAmount)
                .vietQrCode(vietQr)
                .description(description)
                .expirationDate(LocalDateTime.now().plusMinutes(15))
                .build();

    }

    @Override
    @Transactional
    public void processSuccessfulPayment(VietQRCallbackPayload payload) {
        log.info("--- Processing Webhook Payload ---");
        log.info("Received payload: Amount={}, Description='{}', Success={}",
                payload.getAmount(), payload.getDescription(), payload.isSuccess());

        String transactionId = extractTransactionIdFromDescription(payload.getDescription());

        if (transactionId == null) {
            log.warn("Webhook processing stopped: Could not extract a valid transactionId from description '{}'.",
                    payload.getDescription());
            return;
        }
        log.info("Extracted Transaction ID: {}", transactionId);


        List<Payment> payments = paymentRepository.findByStatusAndTransactionId(PaymentStatus.PENDING, transactionId);

        if (payments.isEmpty()) {
            log.warn("Webhook processing stopped: No PENDING payments found for transactionId '{}'. The transaction might have been processed already or is invalid.",
                    transactionId);
            return;
        }
        log.info("Found {} PENDING payment(s) for transactionId '{}'. Proceeding to update.",
                payments.size(), transactionId);

        for (Payment payment : payments) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaymentDate(LocalDateTime.now());

        }

        try {
            paymentRepository.saveAll(payments);
            log.info("Successfully updated {} payment(s) to COMPLETED status in the database.", payments.size());
        } catch (Exception e) {
            log.error("!!!!!!!!!! FAILED to save payments to database!", e);

            throw new RuntimeException("DB update failed during webhook processing", e);
        }

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .transactionId(transactionId)
                .paymentDate(LocalDateTime.now())
                .userId(getUserId())
                .amountPaid(payload.getAmount())
                .build();
        kafkaProducerService.sendPaymentEvent(event);
    }

    private String extractTransactionIdFromDescription(String description) {
        if (description==null || description.isEmpty()){
            return null;
        }
        String prefix = "TT ";
        if (description.startsWith(prefix)) {
            return description.substring(prefix.length()).trim();
        }
        return null;

    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getId();
    }


}
