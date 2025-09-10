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
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;
    @Override
    @Transactional
    public BorrowingCartResponse addItemToCart(AddItemToCartRequest request) {
        // 1. Lấy user và sách, kiểm tra sách
        User currentUser = getCurrentUser();
        BookCopy bookCopy = bookCopyRepository.findByQrCode(request.getQrCode())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_COPY_NOT_FOUND));

        if (bookCopy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new AppException(ErrorCode.BOOK_COPY_NOT_AVAILABLE);
        }

        // 2. TÌM HOẶC TẠO MỚI GIỎ HÀNG (Logic cốt lõi)
        BorrowingCart cart = borrowingCartRepository.findByUser(currentUser)
                .orElseGet(() -> createNewCart(currentUser)); // Chỉ tạo mới nếu user chưa có giỏ nào

        // 3. KIỂM TRA VÀ RESET GIỎ HÀNG CŨ
        // Nếu giỏ hàng đang ở trạng thái kết thúc hoặc đã hết hạn -> Làm mới nó
        if (cart.getStatus() != BorrowingCartStatus.ACTIVE || cart.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetCart(cart);
            entityManager.flush();
        }
        // 4. "CHỐT" TRẠNG THÁI SÁCH VÀ THÊM VÀO GIỎ
        bookCopy.setStatus(BookCopyStatus.IN_CART);
        BorrowingCartItem newItem = BorrowingCartItem.builder()
                .cart(cart)
                .bookCopy(bookCopy)
                .build();
        cart.addItem(newItem);

        // 5. LƯU VÀ TRẢ VỀ
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
        cart.getItems().forEach(item -> item.getBookCopy().setStatus(BookCopyStatus.AVAILABLE));

        // 2. XÓA TẤT CẢ CÁC ITEM CŨ
        // Nhờ có orphanRemoval=true, hành động này sẽ kích hoạt lệnh DELETE trong CSDL
        cart.getItems().clear();

        // 3. Cập nhật trạng thái giỏ hàng
        cart.setStatus(BorrowingCartStatus.EXPIRED);
        borrowingCartRepository.save(cart); // Lưu tất cả thay đổi
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
    private void resetCart(BorrowingCart cart) {
       // log.info("Resetting cart ID {} for a new session.", cart.getId());

        // 1. Trả lại trạng thái cho các sách
        cart.getItems().forEach(item -> item.getBookCopy().setStatus(BookCopyStatus.AVAILABLE));

        // 2. XÓA TẤT CẢ CÁC ITEM CŨ
        cart.getItems().clear();

        // 3. Thiết lập lại các thuộc tính của giỏ hàng
        cart.setStatus(BorrowingCartStatus.ACTIVE);
        cart.setExpiresAt(LocalDateTime.now().plusMinutes(CART_EXPIRATION_MINUTES));
        cart.setConfirmationCode(null);
        cart.setConfirmationCodeExpiresAt(null);

        // Không cần save ở đây, vì phương thức gọi nó (addItemToCart) sẽ save sau
    }
}
