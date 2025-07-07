package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.PendingBorrowResponse;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookCopy;
import com.example.projectlibary.model.PendingBorrow;
import com.example.projectlibary.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PendingBorrowMapper {
    /**
     * Chuyển đổi một đối tượng PendingBorrow entity sang PendingBorrowResponse DTO.
     * <p>
     * <b>Lưu ý cực kỳ quan trọng:</b> Phương thức này cần được gọi trong một ngữ cảnh
     * có transaction đang mở (ví dụ: một phương thức Service được đánh dấu @Transactional)
     * để tránh lỗi LazyInitializationException khi truy cập các đối tượng lồng nhau như
     * pendingBorrow.getUser(), pendingBorrow.getBookCopy(), và pendingBorrow.getBookCopy().getBook().
     * </p>
     *
     * @param pendingBorrow Đối tượng PendingBorrow entity.
     * @return Đối tượng PendingBorrowResponse DTO, hoặc null nếu input là null.
     */
    public PendingBorrowResponse toResponse(PendingBorrow pendingBorrow) {
        // Luôn kiểm tra null để đảm bảo an toàn
        if (pendingBorrow == null) {
            return null;
        }

        // Lấy ra các đối tượng liên quan để tái sử dụng và kiểm tra null
        User user = pendingBorrow.getUser();
        BookCopy bookCopy = pendingBorrow.getBookCopy();
        // Lấy đối tượng Book từ BookCopy, kiểm tra null ở cả hai cấp
        Book book = (bookCopy != null) ? bookCopy.getBook() : null;

        // Sử dụng builder để tạo đối tượng PendingBorrowResponse bất biến
        return PendingBorrowResponse.builder()
                // Map các trường trực tiếp từ PendingBorrow
                .id(pendingBorrow.getId())
                .requestedAt(pendingBorrow.getRequestedAt())
                .expiresAt(pendingBorrow.getExpiresAt())

                // Map các trường "làm phẳng" từ User
                .userId(user != null ? user.getId() : null)
                .userFullName(user != null ? user.getFullName() : "N/A")
                .userStudentId(user != null ? user.getStudentId() : null)

                // Map các trường "làm phẳng" từ BookCopy
                .bookCopyId(bookCopy != null ? bookCopy.getId() : null)
                .bookCopyNumber(bookCopy != null ? bookCopy.getCopyNumber() : "N/A")

                // Map trường "làm phẳng" từ Book (lồng bên trong BookCopy)
                .bookTitle(book != null ? book.getTitle() : "N/A")

                // Hoàn thành việc xây dựng đối tượng
                .build();
    }

    /**
     * Chuyển đổi một danh sách PendingBorrow entity sang danh sách PendingBorrowResponse DTO.
     *
     * @param pendingBorrows Danh sách PendingBorrow entity.
     * @return Danh sách PendingBorrowResponse DTO.
     */
    public List<PendingBorrowResponse> toResponseList(List<PendingBorrow> pendingBorrows) {
        if (pendingBorrows == null) {
            return Collections.emptyList();
        }
        return pendingBorrows.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
