package com.example.projectlibary.service.eventservice;

import com.example.projectlibary.common.ReturnCondition;
import com.example.projectlibary.event.FineIssuedEvent;
import com.example.projectlibary.event.ForgotPasswordEvent;
import com.example.projectlibary.event.UserRegistrationEvent;
import com.example.projectlibary.model.User;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.repository.VerificationTokensRepository;
import com.example.projectlibary.service.AuthenticationService;
import com.example.projectlibary.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

import static com.example.projectlibary.common.ReturnCondition.HEAVILY_DAMAGED;
import static com.example.projectlibary.common.ReturnCondition.LOST;
import static org.thymeleaf.util.NumberUtils.formatCurrency;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {
    private final AuthenticationService authService;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository; // Cần để lấy lại User object
    private final NotificationService notificationService;
    private final VerificationTokensRepository verificationTokensRepository;
    @KafkaListener(topics = "user-registration-events", groupId = "email-notification-group")
    public void handleUserRegistrationEvent(UserRegistrationEvent event) {
        log.info("Received user registration event for email from kafka : Type='{}', Email ='{}'", event.getEvent(), event.getEmail());
        try {
            switch (event.getEvent()) {
                case "REGISTER":
                    handleRegistration(event);
                    break;
                case "VERIFY":
                    handleAccountVerified(event);
                    break;

                default:
                    log.warn("Received unknown event type: {}", event.getEvent());

            }
        } catch (Exception e) {
            log.error("Error processing user registration event for email from kafka : Type: {}", event.getEvent(), e);
        }
    }
    @KafkaListener(topics = "fine-issued-events", groupId = "email-notification-group")
    public void handleFineIssuedEvent(FineIssuedEvent event) {
        log.info("Received FineIssuedEvent for user email: {}", event.getUserEmail());

        String subject = "Thông báo về khoản phạt tại thư viện";

        // Xây dựng nội dung email chi tiết
        String messageText = String.format(
                "Chào %s,\n\n" +
                        "Thư viện xin thông báo một khoản phạt đã được ghi nhận cho tài khoản của bạn.\n\n" +
                        "--- Chi tiết ---\n" +
                        "Sách: %s (Mã bản sao: %s)\n" +
                        "Lý do: %s\n" +
                        "Số tiền phạt: %s\n" +
                        "Ghi chú từ thủ thư: %s\n\n" +
                        "Vui lòng truy cập trang thanh toán của bạn trong ứng dụng để xem chi tiết và hoàn tất thanh toán.\n" +
                        "ID thanh toán: %d\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ thư viện.",
                event.getUserFullName(),
                event.getBookTitle(),
                event.getBookCopyNumber(),
                getReasonInVietnamese(event.getReason()), // Hàm dịch lý do
                formatCurrency(event.getFineAmount()),
                event.getLibrarianNotes() != null ? event.getLibrarianNotes() : "Không có",
                event.getPaymentId()
        );

        sendEmail(event.getUserEmail(), subject, messageText);
    }
    @KafkaListener(topics = "forgot-password-events", groupId = "email-notification-group")
    public void handleForgotPasswordEvent(ForgotPasswordEvent event) {
        User user = findByUserId(event.getUserId());
        if(user==null) {
            return;
        }
        verificationTokensRepository.findByUser(user).ifPresent(verificationTokensRepository::delete);
        String token = UUID.randomUUID().toString();
        authService.createVerificationTokenForUser(user, token);
        String recipientAddress = user.getEmail();
        String subject= "Đổi mật khẩu";
        String confirmationUrl = event.getAppUrl() + "/api/auth/reset-password?token=" + token;
        String messageText = "Thank you for registering. Please click the link below to activate your account:";
        sendEmail(recipientAddress, subject, messageText + "\r\n" + confirmationUrl);

    }
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "N/A";
        return amount.toPlainString() + " VND";
    }
    private String getReasonInVietnamese(ReturnCondition reason) {
        switch (reason) {
            case SLIGHTLY_DAMAGED: return "Sách bị hỏng nhẹ";
            case HEAVILY_DAMAGED: return "Sách bị hỏng nặng";
            case LOST: return "Báo mất sách";
            default: return "Không xác định";
        }
    }

    private void handleAccountVerified(UserRegistrationEvent event) {
        User user = findByUserId(event.getUserId());
        if (user == null) {
            return;
        }
        String recipientAddress = user.getEmail();
        String subject = "Chào mừng đến với Thư viện";
        String messageText = "Xin chào " + (user.getUsername()!=null ? user.getUsername():"bạn") + "!\n\n"
                + "Chào mừng bạn đã gia nhập! Tài khoản của bạn đã được xác minh thành công.\n\n"
                + "Chúng tôi khuyến khích bạn cập nhật hồ sơ cá nhân để có được trải nghiệm tốt nhất tại thư viện. "
                + "Bạn có thể thực hiện điều đó bằng cách truy cập trang hồ sơ của mình tại đây: [Liên kết đến trang Hồ sơ]\n\n"
                + "Chúc bạn đọc sách vui vẻ!\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ Thư viện";
        sendEmail(recipientAddress, subject, messageText);
        notificationService.createWelcomeNotification(user);
    }


    private void handleRegistration(UserRegistrationEvent event) {
        User user = findByUserId(event.getUserId());
        if (user == null) return;

        String token = UUID.randomUUID().toString();
        authService.createVerificationTokenForUser(user, token);

        String recipientAddress = user.getEmail();
        String subject = "Xác nhận đăng ký tài khoản";
        String confirmationUrl = event.getAppUrl() + "/api/auth/registrationConfirm?token=" + token;
        String messageText = "Cảm ơn bạn đã đăng ký. Vui lòng nhấp vào liên kết bên dưới để kích hoạt tài khoản của bạn.\n:";

        sendEmail(recipientAddress, subject, messageText + "\r\n" + confirmationUrl);
    }
    private User findByUserId(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("User with ID {} not found.", userId);
        }
        return user;
    }
    private void sendEmail(String recipientAddress, String subject, String messageText) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(recipientAddress);
            email.setSubject(subject);
            email.setText(messageText);
            mailSender.send(email);
            log.info("Email sent successfully to {}", recipientAddress);
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", recipientAddress, e.getMessage());
            // Ném exception để Kafka có thể retry message này
            throw new RuntimeException("Failed to send email to " + recipientAddress, e);
        }
    }
}