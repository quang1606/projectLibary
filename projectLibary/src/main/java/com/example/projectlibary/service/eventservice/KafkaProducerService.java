package com.example.projectlibary.service.eventservice;

import com.example.projectlibary.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {
    //Ten topic
    private static final String TOPIC_NAME = "book_events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void senBookEvent(BookSyncEvent event) {
        try {
            kafkaTemplate.send(TOPIC_NAME, String.valueOf(event.getId()), event);
            log.info("Sent book event to Kafka topic '{}': {}", TOPIC_NAME, event.getId());
        }catch (Exception e) {
            log.error("Error sending event to topic", e);
        }
    }
    public void sendUserRegistrationEvent(UserRegistrationEvent event) {
        try {
            kafkaTemplate.send("user-registration-events", String.valueOf(event.getUserId()), event);
            log.info("Sent user registration event for email: {}", event.getEmail());
        }catch (Exception e) {
            log.error("Error sending event to topic", e);
        }
    }
    public void sendLoanConfirmationEvent(LoanConfirmationEvent event) {
        try {
            kafkaTemplate.send("loan-confirmation-events", String.valueOf(event.getUserId()), event);

        }catch (Exception e) {
            log.error("Error sending event to topic", e);
        }
    }

    public void sendBookCopyId(BookStatusChangedEvent event) {
        try {
            kafkaTemplate.send("cart-item-added-events",String.valueOf(event.getUserId()), event);
        } catch (Exception e) {
            log.error("Error sending event to topic", e);
        }
    }
    public void sendFineIssuedEvent(FineIssuedEvent event) {
        try {
            kafkaTemplate.send("fine-issued-events", String.valueOf(event.getUserId()), event);
        }catch (Exception e) {
            log.error("Error sending event to topic", e);
        }
    }
}
