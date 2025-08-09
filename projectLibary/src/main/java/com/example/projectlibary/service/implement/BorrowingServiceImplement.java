package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.common.BorrowingCartStatus;
import com.example.projectlibary.dto.reponse.BorrowingCartResponse;
import com.example.projectlibary.dto.request.AddItemToCartRequest;
import com.example.projectlibary.event.BookStatusChangedEvent;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.BorrowingCartMapper;
import com.example.projectlibary.model.BookCopy;
import com.example.projectlibary.model.BorrowingCart;
import com.example.projectlibary.model.BorrowingCartItem;
import com.example.projectlibary.model.User;
import com.example.projectlibary.repository.BookCopyRepository;
import com.example.projectlibary.repository.BorrowingCartItemRepository;
import com.example.projectlibary.repository.BorrowingCartRepository;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.service.BorrowingService;
import com.example.projectlibary.service.eventservice.KafkaProducerService;
import com.example.projectlibary.utils.QRCodeUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImplement implements BorrowingService {
    private static final int CART_EXPIRATION_MINUTES = 30;
    private static final int CONFIRMATION_CODE_EXPIRATION_MINUTES = 5;
    private final BookCopyRepository bookCopyRepository;
    private final UserRepository userRepository;
    private final BorrowingCartRepository borrowingCartRepository;
    private final BorrowingCartMapper borrowingCartMapper;
    private final QRCodeUtil qrCodeUtil;
    private final BorrowingCartItemRepository borrowingCartItemRepository;
    private final KafkaProducerService kafkaProducerService;
    @Override
    @Transactional
    public BorrowingCartResponse addItemToCart(AddItemToCartRequest request) {
        User currentUser = getCurrentUser();
        BookCopy bookCopy = bookCopyRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_COPY_NOT_FOUND));
        if (bookCopy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new AppException(ErrorCode.BOOK_COPY_NOT_AVAILABLE);
        }
        bookCopy.setStatus(BookCopyStatus.IN_CART);
        // (Thêm các kiểm tra khác: giới hạn mượn, sách quá hạn...)
        Optional<BorrowingCart> activeCartOpt = borrowingCartRepository.findByUser_IdAndStatus(currentUser.getId(), BorrowingCartStatus.ACTIVE);

        BorrowingCart cart;
        if (activeCartOpt.isPresent()) {
            cart = activeCartOpt.get();

            if (cart.getExpiresAt().isBefore(LocalDateTime.now())) {
                // Nếu giỏ hàng đã hết hạn, xử lý nó và yêu cầu người dùng tạo lại
                expireCart(cart); // Gọi hàm private để dọn dẹp

                throw new AppException(ErrorCode.BORROWING_CART_EXPIRED);
            }
            // Nếu còn hạn, tiếp tục dùng giỏ hàng này
        } else {
            // Nếu không có giỏ hàng ACTIVE nào, tạo một cái mới
            cart = createNewCart(currentUser);
        }
        BorrowingCartItem newItem = BorrowingCartItem.builder()
                .cart(cart)
                .bookCopy(bookCopy)
                .build();
        cart.addItem(newItem);
        BorrowingCart savedCart = borrowingCartRepository.save(cart);
        BookStatusChangedEvent event = BookStatusChangedEvent.builder()
                .bookId(bookCopy.getId())
                .userId(currentUser.getId())
                .addedAt(LocalDateTime.now())
                .build();
        kafkaProducerService.sendBookCopyId(event);
        return borrowingCartMapper.toBorrowingCartResponse(savedCart);
    }


    @Override
    public BorrowingCartResponse getAllCartItems() {
        User currentUser = getCurrentUser();
        BorrowingCart cart = findCartForUser(currentUser);

        if (cart.getExpiresAt().isBefore(LocalDateTime.now())) {
            expireCart(cart);
            throw new AppException(ErrorCode.BORROWING_CART_EXPIRED);
        }
        return borrowingCartMapper.toBorrowingCartResponse(cart) ;
    }

    @Override
    @Transactional
    public BorrowingCartResponse generateConfirmation() {
        User currentUser = getCurrentUser();
        BorrowingCart cart = findCartForUser(currentUser);
        if(cart.getItems().isEmpty()){
            throw new AppException(ErrorCode.BORROWING_CART_IS_EMPTY);
        }
        if (cart.getExpiresAt().isBefore(LocalDateTime.now())) {
            expireCart(cart);
            throw new AppException(ErrorCode.BORROWING_CART_EXPIRED);
        }
        String code = generateRandomNumericString(6);
        // Tạo QR code ảnh base64 từ code
        String qrCodeImageBase64 = qrCodeUtil.generateQRCodeBase64(code,150,150);
        cart.setStatus(BorrowingCartStatus.PENDING_CONFIRMATION);
        cart.setConfirmationCode(code);
        cart.setConfirmationCodeExpiresAt(LocalDateTime.now().plusMinutes(CONFIRMATION_CODE_EXPIRATION_MINUTES));
        BorrowingCart savedCart = borrowingCartRepository.save(cart);
        BorrowingCartResponse response = borrowingCartMapper.toBorrowingCartResponse(savedCart);
        response.setQrCodeImageBase64(qrCodeImageBase64);

        return response;
    }

    @Override
    @Transactional
    public BorrowingCartResponse removeItemFromCart(Long bookCopyId) {
        User currentUser = getCurrentUser();
        BorrowingCart cart = findCartForUser(currentUser);

        // Tìm item cần xóa trong giỏ hàng
        BorrowingCartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getBookCopy().getId().equals(bookCopyId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_IN_CART));

        // Lấy lại BookCopy và trả về trạng thái AVAILABLE
        BookCopy bookCopy = itemToRemove.getBookCopy();
        bookCopy.setStatus(BookCopyStatus.AVAILABLE);

        cart.removeItem(itemToRemove);

        BorrowingCart updatedCart = borrowingCartRepository.save(cart);
        BookStatusChangedEvent event = BookStatusChangedEvent.builder()
                .bookId(bookCopy.getId())
                .userId(currentUser.getId())
                .addedAt(LocalDateTime.now())
                .build();
        kafkaProducerService.sendBookCopyId(event);
        return borrowingCartMapper.toBorrowingCartResponse(updatedCart);
    }



    private void expireCart(BorrowingCart cart) {
        cart.setStatus(BorrowingCartStatus.EXPIRED);
        for (BorrowingCartItem item : cart.getItems()) {
            item.getBookCopy().setStatus(BookCopyStatus.AVAILABLE);
        }

        borrowingCartRepository.save(cart);
    }
    private String generateRandomNumericString(int length) {
        Random random = new Random();
        String qrcode;
        do {
             qrcode = random.ints(length,0,10).mapToObj(Integer::toString).collect(Collectors.joining());

        }while (borrowingCartRepository.existsByConfirmationCode(qrcode));
        return qrcode;
        }

    private BorrowingCart findCartForUser(User currentUser) {
        return borrowingCartRepository.findByUser_IdAndStatus(currentUser.getId(),BorrowingCartStatus.ACTIVE)
                .orElseThrow(()-> new AppException(ErrorCode.BORROWING_CART_NOT_FOUND));
    }


    private BorrowingCart createNewCart(User currentUser) {
        return BorrowingCart.builder()
                .user(currentUser)
                .status(BorrowingCartStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(CART_EXPIRATION_MINUTES))
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByEmail(username).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
