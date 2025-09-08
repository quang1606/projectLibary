package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.NotificationType;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.UserNotificationResponse;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.UserNotificationMapper;
import com.example.projectlibary.model.*;
import com.example.projectlibary.repository.UserNotificationRepository;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationServiceImplement implements NotificationService {
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final UserNotificationMapper userNotificationMapper;

    @Override
    @Transactional
    public void createWelcomeNotification(User user) {
        String message = "Chào mừng bạn đến với thư viện! Hãy cập nhật hồ sơ cá nhân để có trải nghiệm tốt nhất.";
        UserNotification userNotification = UserNotification.builder()
                .user(user)
                .message(message)
                .type(NotificationType.GENERAL)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
        try {
            userNotificationRepository.save(userNotification);
            log.info("Successfully created a welcome notification for user ID: {}", user.getId());
        }catch (Exception e){
            log.error("Failed to create welcome notification for user ID: {}", user.getId(), e);

        }
    }

    @Override
    public void createLoanSuccessNotification(User user, List<BorrowingCartItem> verifiedItems) {
        String message = "Bạn đã mượn thành công " + verifiedItems.size() + " cuốn sách.";
        UserNotification userNotification = UserNotification.builder()
                .user(user)
                .message(message)
                .type(NotificationType.GENERAL)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
        try {
            userNotificationRepository.save(userNotification);

        }catch (Exception e){
            log.error("Failed to create welcome notification for user ID: {}", user.getId(), e);

        }
    }

    @Override
    public void createReturnSuccessNotification(BookLoan loan) {
        String bookTitle = loan.getBookCopy().getBook().getTitle();
        String message = String.format("Bạn đã trả thành công cuốn sách '%s'. Cảm ơn bạn!", bookTitle);
        UserNotification userNotification = UserNotification.builder()
                .user(loan.getUser())
                .message(message)
                .type(NotificationType.RETURN_CONFIRMED)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
        try {
            userNotificationRepository.save(userNotification);

        }catch (Exception e){
            log.error("Failed to create welcome notification for user ID: {}", loan.getUser(), e);

        }
    }

    @Override
    public void createReturnSlightlyDamagedNotification(BookLoan loan, BigDecimal fineAmount) {
        String bookTitle = loan.getBookCopy().getBook().getTitle();
        String message = String.format(
                "Thủ thư đã xác nhận bạn trả sách '%s' trong tình trạng hỏng nhẹ. Một khoản phạt %s đã được ghi nhận. Vui lòng liên hệ thư viện để biết thêm chi tiết.",
                bookTitle,
                formatCurrency(fineAmount) // Hàm format tiền tệ
        );
        UserNotification userNotification = UserNotification.builder()
                .user(loan.getUser())
                .message(message)
                .type(NotificationType.RETURN_CONFIRMED)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
        try {
            userNotificationRepository.save(userNotification);

        }catch (Exception e){
            log.error("Failed to create welcome notification for user ID: {}", loan.getUser(), e);

        }
    }

    @Override
    public void createReturnLostOrHeavilyDamagedNotification(BookLoan loan, BigDecimal replacementCost) {
        String bookTitle = loan.getBookCopy().getBook().getTitle();
        String message = String.format(
                "Sách '%s' bạn mượn đã được ghi nhận là bị hỏng nặng. Bạn cần thanh toán chi phí đền bù là %s. Vui lòng liên hệ thư viện để xử lý.",
                bookTitle,
                formatCurrency(replacementCost)
        );
        UserNotification userNotification = UserNotification.builder()
                .user(loan.getUser())
                .message(message)
                .type(NotificationType.RETURN_CONFIRMED)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
        try {
            userNotificationRepository.save(userNotification);

        }catch (Exception e){
            log.error("Failed to create welcome notification for user ID: {}", loan.getUser(), e);

        }

    }


    @Override
    public void createAlertNotification(BookLoan loan) {
        String bookTitle = loan.getBookCopy().getBook().getTitle();
        String message = String.format("Cuốn sác '%s' còn một ngày nữa là hết hạn bạn hãy trả đúng thời gian quy định. Cảm ơn bạn!", bookTitle);
        UserNotification userNotification = UserNotification.builder()
                .user(loan.getUser())
                .message(message)
                .type(NotificationType.DUE_DATE_REMINDER)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();
        try {
            userNotificationRepository.save(userNotification);

        }catch (Exception e){
            log.error("Failed to create welcome notification for user ID: {}", loan.getUser(), e);

        }
    }


    @Override
    public PageResponse<UserNotificationResponse> getAllNotifications(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<UserNotification> userNotifications = userNotificationRepository.findByUser_Id(userDetails.getId(),pageable);
        List<UserNotificationResponse> userNotificationResponses = userNotifications.getContent().stream()
                .map(userNotificationMapper::toResponse).toList();
        return PageResponse.from(userNotifications, userNotificationResponses);
    }

    @Override
    public UserNotificationResponse getNotificationById(long id) {
        UserNotification userNotification = userNotificationRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        return userNotificationMapper.toResponse(userNotification);
    }
    private String formatCurrency(BigDecimal amount) {
        // Implement logic để format tiền tệ theo định dạng bạn muốn, ví dụ: "100,000 VND"
        if (amount == null) return "N/A";
        return amount.toPlainString() + " VND";
    }

}
