package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.UserNotificationResponse;
import com.example.projectlibary.model.UserNotification;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserNotificationMapper {

    /**
     * Chuyển đổi một đối tượng UserNotification entity sang UserNotificationResponse DTO.
     * <p>
     * DTO này không yêu cầu truy cập sâu vào các đối tượng liên quan (ngoại trừ chính
     * UserNotification), do đó ít có nguy cơ gặp lỗi LazyInitializationException hơn
     * so với các mapper khác, nhưng vẫn nên được gọi từ tầng Service.
     * </p>
     *
     * @param notification Đối tượng UserNotification entity.
     * @return Đối tượng UserNotificationResponse DTO, hoặc null nếu input là null.
     */
    public UserNotificationResponse toResponse(UserNotification notification) {
        // Luôn kiểm tra null để đảm bảo an toàn
        if (notification == null) {
            return null;
        }

        // Sử dụng builder để tạo đối tượng UserNotificationResponse bất biến
        return UserNotificationResponse.builder()
                // Map tất cả các trường trực tiếp từ UserNotification
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead()) // Sử dụng isRead() cho kiểu boolean
                .sentAt(notification.getSentAt())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                // Hoàn thành việc xây dựng đối tượng
                .build();
    }

    /**
     * Chuyển đổi một danh sách UserNotification entity sang danh sách UserNotificationResponse DTO.
     * <p>
     * Rất hữu ích cho API lấy danh sách thông báo của người dùng.
     * </p>
     *
     * @param notifications Danh sách UserNotification entity.
     * @return Danh sách UserNotificationResponse DTO.
     */
    public List<UserNotificationResponse> toResponseList(List<UserNotification> notifications) {
        if (notifications == null) {
            return Collections.emptyList();
        }
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
