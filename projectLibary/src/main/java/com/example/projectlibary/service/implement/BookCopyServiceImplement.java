package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.dto.reponse.BookCopyResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateBookCopyRequest;
import com.example.projectlibary.dto.request.UpdateBookCopyRequest;
import com.example.projectlibary.event.BookStatusChangedEvent;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.BookCopyMapper;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookCopy;
import com.example.projectlibary.model.User;
import com.example.projectlibary.repository.BookCopyRepository;
import com.example.projectlibary.repository.BookRepository;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.service.BookCopyService;
import com.example.projectlibary.service.eventservice.KafkaProducerService;
import com.example.projectlibary.utils.QRCodeUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookCopyServiceImplement implements BookCopyService {
private final UserRepository userRepository;
private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookCopyMapper bookCopyMapper;
    private final QRCodeUtil qrCodeUtil;
    private final KafkaProducerService kafkaProducerService;
    @Override
    @Transactional
    public List<BookCopyResponse> createBookCopies(CreateBookCopyRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Book  book = bookRepository.findById(request.getBookId()).orElseThrow(()-> new AppException(ErrorCode.BOOK_NOT_FOUND));
        List<BookCopy> bookCopies = new ArrayList<>();
        for (int i=0; i< request.getNumberOfCopies();i++){
            String uniqueCopyNumber = generateUniqueCopyNumber(book.getIsbn());
            String uniqueQrCode = uniqueCopyNumber;
            BookCopy bookCopy = BookCopy.builder()
                    .book(book)
                    .copyNumber(uniqueCopyNumber)
                    .qrCode(uniqueQrCode)
                    .status(BookCopyStatus.AVAILABLE)
                    .location(request.getLocation())
                    .createdBy(user)
                    .build();
            bookCopies.add(bookCopy);
        }
        List<BookCopy> copies = bookCopyRepository.saveAll(bookCopies);
        if (!copies.isEmpty()) {

            BookStatusChangedEvent event = BookStatusChangedEvent.builder()
                    .bookId(book.getId())
                    .userId(user.getId())
                    .addedAt(LocalDateTime.now())
                    .build();
            kafkaProducerService.sendBookCopyId(event);
        }

        return bookCopyMapper.toBookCopyResponseList(copies);
    }

    @Override
    public PageResponse<BookCopyResponse> getAllBookCopy(Long id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookCopy> bookCopies = bookCopyRepository.findByBook_Id(id, pageable);
        List<BookCopyResponse> bookCopyResponses = bookCopyMapper.toBookCopyResponseList(bookCopies.getContent());
        return PageResponse.from(bookCopies, bookCopyResponses);
    }

    @Override
    @Transactional
    public BookCopyResponse updateBookCopies(UpdateBookCopyRequest request, Long id) {

        BookCopy bookCopy = bookCopyRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_COPY_NOT_FOUND));
        if(request.getLocation() != null && !request.getLocation().isBlank()){
            bookCopy.setStatus(request.getStatus());
        }
        if (request.getStatus() != null){
            bookCopy.setLocation(request.getLocation());
        }
        bookCopyRepository.save(bookCopy);
        return bookCopyMapper.toBookCopyResponse(bookCopy);
    }



    @Override
    @Transactional
    public void deleteBookCopies(Long copyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        BookCopy bookCopyToDelete = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_COPY_NOT_FOUND));


        if (bookCopyToDelete.getStatus() == BookCopyStatus.BORROWED) {
            throw new AppException(ErrorCode.CANNOT_DELETE_COPY_BORROWED);
        }

        if (bookCopyToDelete.getStatus() == BookCopyStatus.IN_CART) {
            throw new AppException(ErrorCode.CANNOT_DELETE_COPY_RESERVED);
        }

        // Nếu tất cả kiểm tra đều qua, thực hiện "xóa mềm"
        // Bằng cách cập nhật trạng thái của nó
        bookCopyToDelete.setStatus(BookCopyStatus.DISCARDED);
        bookCopyToDelete.setLocation("N/A");

        bookCopyRepository.save(bookCopyToDelete);
        BookStatusChangedEvent event = BookStatusChangedEvent.builder()
                .bookId(bookCopyToDelete.getId())
                .userId(user.getId())
                .addedAt(LocalDateTime.now())
                .build();
        kafkaProducerService.sendBookCopyId(event);

        log.info("Successfully soft-deleted (discarded) book copy with ID: {}", copyId);
    }

    @Override
    public String getQRCodeImage(Long copyId) {
        BookCopy bookCopy = bookCopyRepository.findById(copyId).orElseThrow(() -> new AppException(ErrorCode.BOOK_COPY_NOT_FOUND));
        String qrData = bookCopy.getQrCode();
        return qrCodeUtil.generateQRCodeBase64(qrData, 200, 200);

    }



    private String generateUniqueCopyNumber(String isbn) {
        String uniquePart;
        String potentialCopyNumber;
        do {

            uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            potentialCopyNumber = isbn + "-" + uniquePart;
        } while (bookCopyRepository.existsByCopyNumber(potentialCopyNumber)); // Kiểm tra sự tồn tại

        return potentialCopyNumber;
    }



}
