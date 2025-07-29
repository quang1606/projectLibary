package com.example.projectlibary.service.eventservice;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.common.BookLoanStatus;
import com.example.projectlibary.common.BorrowingCartStatus;
import com.example.projectlibary.event.LoanConfirmationEvent;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.model.*;
import com.example.projectlibary.repository.BookCopyRepository;
import com.example.projectlibary.repository.BookLoanRepository;
import com.example.projectlibary.repository.BorrowingCartRepository;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j

public class LoanProcessingService {
    private final BorrowingCartRepository borrowingCartRepository;
    private final UserRepository userRepository;
    private final BookLoanRepository bookLoanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final NotificationService notificationService;
    @KafkaListener(topics = "loan-confirmation-events", groupId = "loan-processing-group")
    @Transactional
    public void receiveLoanConfirmationEvent(LoanConfirmationEvent event) {
        log.info("Processing loan confirmation for cart ID: {}", event.getCartId());
        try {

            User user= userRepository.findById(event.getUserId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            BorrowingCart cart = borrowingCartRepository.findById(event.getCartId()).orElseThrow(()->new AppException(ErrorCode.BORROWING_CART_NOT_FOUND));
            List<BorrowingCartItem> verifiedItems  = cart.getItems().stream().filter(BorrowingCartItem::isVerifiedByLibrarian).toList();
            List<BorrowingCartItem> unVerifiedItems =cart.getItems().stream().filter(verifiedItem -> !verifiedItem.isVerifiedByLibrarian()).toList();
            for (BorrowingCartItem item : verifiedItems) {
                BookCopy bookCopy = item.getBookCopy();
                BookLoan bookLoan = BookLoan.builder()
                        .user(cart.getUser())
                        .bookCopy(bookCopy)
                        .borrowedAt(event.getConfirmationDate())
                        .dueDate(LocalDate.now().plusWeeks(2))
                        .status(BookLoanStatus.BORROWED)
                        .fineAmount(BigDecimal.ZERO)
                        .updatedBy(user)
                        .createdBy(user)
                        .librarian(user)
                        .build();
                bookLoanRepository.save(bookLoan);
                bookCopy.setStatus(BookCopyStatus.BORROWED);

            }
            // 2. Xử lý các sách không được mượn (trả về kệ)
            for (BorrowingCartItem item : unVerifiedItems) {
                item.getBookCopy().setStatus(BookCopyStatus.AVAILABLE);
                bookCopyRepository.save(item.getBookCopy());
            }
            cart.setStatus(BorrowingCartStatus.COMPLETED);

            log.info("Successfully processed loan for cart ID: {}", event.getCartId());

            // 4. (Tùy chọn) Gửi thông báo cho người dùng
            notificationService.createLoanSuccessNotification(cart.getUser(), verifiedItems);

        }catch (Exception e) {
            log.error("Failed to process loan confirmation for cart ID: {}. Will retry.", event.getCartId(), e);
            // Ném ra exception để Spring Kafka tự động retry message này
            throw new RuntimeException("Processing failed for cart " + event.getCartId(), e);
        }


    }
}
