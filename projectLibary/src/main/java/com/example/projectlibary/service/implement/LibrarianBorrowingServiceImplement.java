package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.common.BorrowingCartStatus;
import com.example.projectlibary.dto.reponse.BorrowingCartResponse;
import com.example.projectlibary.dto.request.ConfirmLoanRequest;
import com.example.projectlibary.dto.request.VerifyCartItemRequest;
import com.example.projectlibary.event.LoanConfirmationEvent;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.BorrowingCartMapper;
import com.example.projectlibary.model.BookCopy;
import com.example.projectlibary.model.BorrowingCart;
import com.example.projectlibary.model.BorrowingCartItem;
import com.example.projectlibary.model.User;
import com.example.projectlibary.repository.BookCopyRepository;
import com.example.projectlibary.repository.BorrowingCartRepository;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.service.eventservice.KafkaProducerService;
import com.example.projectlibary.service.LibrarianBorrowingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LibrarianBorrowingServiceImplement implements LibrarianBorrowingService {
    private final BorrowingCartRepository borrowingCartRepository;
    private final BorrowingCartMapper borrowingCartMapper;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void borrowingCart() {
        List<BorrowingCart> borrowingCarts = borrowingCartRepository.findByStatusAndExpiresAtBefore(BorrowingCartStatus.ACTIVE, LocalDateTime.now());
        if (!borrowingCarts.isEmpty()) {
            for (BorrowingCart borrowingCart : borrowingCarts) {
                expireCart(borrowingCart);
            }
        }
        List<BorrowingCart> expiredPendingCarts = borrowingCartRepository
                .findByStatusAndConfirmationCodeExpiresAtBefore(BorrowingCartStatus.PENDING_CONFIRMATION, LocalDateTime.now());
        if(!expiredPendingCarts.isEmpty()) {
            for (BorrowingCart borrowingCart : expiredPendingCarts) {
                revertCartToActive(borrowingCart);
            }
        }
    }

    @Override
    public BorrowingCartResponse getCartForVerificationCode(ConfirmLoanRequest confirmationCode) {
        BorrowingCart cart = findActiveCartByCode(confirmationCode);
        if(cart.getConfirmationCodeExpiresAt().isBefore(LocalDateTime.now())){
            revertCartToActive(cart);
            throw new AppException(ErrorCode.EXPIRED_VERIFICATION_TOKEN);
        }
        return borrowingCartMapper.toBorrowingCartResponse(cart);
    }
    private void expireCart(BorrowingCart cart) {
        cart.setStatus(BorrowingCartStatus.EXPIRED);
        cart.getItems().forEach(item -> item.getBookCopy().setStatus(BookCopyStatus.AVAILABLE));
        borrowingCartRepository.save(cart);
    }


    private void revertCartToActive(BorrowingCart cart) {
        cart.getItems().forEach(item -> item.getBookCopy().setStatus(BookCopyStatus.AVAILABLE));
        cart.setStatus(BorrowingCartStatus.ACTIVE);
        cart.setConfirmationCode(null);
        cart.setConfirmationCodeExpiresAt(null);
        borrowingCartRepository.save(cart);
    }

    @Override
    @Transactional
    public BorrowingCartResponse verifyCartItem(VerifyCartItemRequest verifyCartItemRequest, String id) {
        BorrowingCart cart = borrowingCartRepository.findByConfirmationCode(id)
                .orElseThrow(()->new AppException(ErrorCode.BORROWING_CART_NOT_FOUND));
        if(cart.getConfirmationCodeExpiresAt().isBefore(LocalDateTime.now())){
            revertCartToActive(cart);
            throw new AppException(ErrorCode.EXPIRED_VERIFICATION_TOKEN);
        }
        cart.setStatus(BorrowingCartStatus.PROCESSING);
        BookCopy bookCopy = bookCopyRepository.findByQrCode(verifyCartItemRequest.getQrCode())
                .orElseThrow(()->new AppException(ErrorCode.BOOK_COPY_NOT_FOUND));
        BorrowingCartItem borrowingCartItem = cart.getItems().stream()
                .filter(item->item.getBookCopy().getId().equals(bookCopy.getId()))
                .findFirst()
                .orElseThrow(()->new AppException(ErrorCode.ITEM_NOT_IN_CART));
        if(borrowingCartItem.isVerifiedByLibrarian()){
            throw new AppException(ErrorCode.SCANNED_AND_CONFIRMED);
        }
        borrowingCartItem.setVerifiedByLibrarian(true);
        BorrowingCart updateCart = borrowingCartRepository.save(cart);
        return borrowingCartMapper.toBorrowingCartResponse(updateCart);
    }

    @Override

    public void completeLoanSession(Long cartId) {
        User user = getCurrentUser();
        BorrowingCart cart = borrowingCartRepository.findById(cartId)
                .orElseThrow(()->new AppException(ErrorCode.BORROWING_CART_NOT_FOUND));
        boolean cartItem = cart.getItems().stream().anyMatch(BorrowingCartItem::isVerifiedByLibrarian);
        if(!cartItem){
            revertCartToActive(cart);
            throw new AppException(ErrorCode.NO_ITEMS_VERIFIED);
        }

        borrowingCartRepository.save(cart);
        LoanConfirmationEvent event = LoanConfirmationEvent.builder()
                .cartId(cart.getId())
                .userId(user.getId())
                .confirmationDate(LocalDateTime.now())
                .build();

        kafkaProducerService.sendLoanConfirmationEvent(event);

    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private BorrowingCart findActiveCartByCode(ConfirmLoanRequest confirmationCode) {
        return borrowingCartRepository.findByConfirmationCode(confirmationCode.getConfirmationCode())
                .orElseThrow(()->new AppException(ErrorCode.BORROWING_CART_NOT_FOUND));
    }

}
