package com.example.projectlibary.service.eventservice;

import com.example.projectlibary.dto.reponse.WebSocketNotification;
import com.example.projectlibary.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumerService {
    private final SimpMessagingTemplate messagingTemplate;
    @KafkaListener(topics = "payments-completed-events", groupId = "websocket-notification-group")
    public void processPaymentCompletedEvent(PaymentCompletedEvent event) {
        String message = String.format("Thanh toán cho giao dịch %s trị giá %s VND đã thành công!",
                event.getTransactionId(), event.getAmountPaid().toPlainString());
        WebSocketNotification notification = WebSocketNotification.builder()
                .type("PAYMENT_SUCCESS")
                .message(message)
                .timestamp(event.getPaymentDate())
                .build();

        // Gửi message đến topic riêng của người dùng.
        messagingTemplate.convertAndSendToUser(
                String.valueOf(event.getUserId()),
                "/topic/notifications",
                notification
        );

        log.info("Sent WebSocket notification for PAYMENT_SUCCESS to user ID: {}", event.getUserId());
    }
}
