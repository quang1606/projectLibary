package com.example.projectlibary.service;

import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.UserNotificationResponse;
import com.example.projectlibary.model.BookLoan;
import com.example.projectlibary.model.BorrowingCartItem;
import com.example.projectlibary.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface NotificationService {
    void createWelcomeNotification(User user);

    void createLoanSuccessNotification(User user, List<BorrowingCartItem> verifiedItems);

    /**
     * Thông báo trả sách thành công (tình trạng bình thường).
     */
    void createReturnSuccessNotification(BookLoan loan);

    /**
     * Thông báo trả sách bị hỏng nhẹ và có thể có phạt.
     */
    void createReturnSlightlyDamagedNotification(BookLoan loan, BigDecimal fineAmount);

    /**
     * Thông báo sách bị hỏng nặng hoặc mất và phải đền bù.
     */
    void createReturnLostOrHeavilyDamagedNotification(BookLoan loan, BigDecimal replacementCost);
    PageResponse<UserNotificationResponse> getAllNotifications(int page, int size);

    UserNotificationResponse getNotificationById(long id);

}
