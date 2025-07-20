package com.example.projectlibary.service;

import com.example.projectlibary.event.UserRegistrationEvent;
import com.example.projectlibary.model.User;
import com.example.projectlibary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {
    private final AuthenticationService authService;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository; // Cần để lấy lại User object

    @KafkaListener(topics = "user-registration-events", groupId = "email-notification-group")
    public void handleUserRegistrationEvent(UserRegistrationEvent event) {
        log.info("Received user registration event for email: {}", event.getEmail());

        // Lấy lại đối tượng User đầy đủ từ CSDL
        // Cần thiết vì Consumer có thể là một service riêng, không có User object
        User user = userRepository.findById(event.getUserId())
                .orElse(null);

        if (user == null) {
            log.error("User with ID {} not found, cannot send verification email.", event.getUserId());
            return;
        }

        String token = UUID.randomUUID().toString();
        authService.createVerificationTokenForUser(user, token);

        String recipientAddress = user.getEmail();
        String subject = "Account Registration Confirmation";
        String confirmationUrl = event.getAppUrl() + "/api/auth/registrationConfirm?token=" + token;
        String messageText = "Thank you for registering. Please click the link below to activate your account:";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(messageText + "\r\n" + confirmationUrl);

        try {
            mailSender.send(email);
            log.info("Verification email sent to {}", recipientAddress);
        } catch (Exception e) {
            log.error("Error sending verification email to {}: {}", recipientAddress, e.getMessage());
            // Với Kafka, bạn có thể cấu hình để tự động retry nếu bước này thất bại
            throw new RuntimeException("Failed to send email, will retry.", e);
        }
    }
}
