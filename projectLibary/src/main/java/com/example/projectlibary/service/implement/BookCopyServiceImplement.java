package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.dto.reponse.BookCopyResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateBookCopyRequest;
import com.example.projectlibary.dto.request.UpdateBookCopyRequest;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    @Override
    public List<BookCopyResponse> createBookCopies(CreateBookCopyRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Book  book = bookRepository.findById(request.getBookId()).orElseThrow(()-> new AppException(ErrorCode.BOOK_NOT_FOUND));
        List<BookCopy> bookCopies = new ArrayList<>();
        for (int i=0; i< request.getNumberOfCopies();i++){
            String uniqueCopyNumber = generateUniqueCopyNumber(book.getIsbn());
            String uniqueQrCode = generateUniqueQrCode(uniqueCopyNumber);
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

    // service/implement/BookCopyServiceImplement.java

    @Override
    @Transactional
    public void deleteBookCopies(Long copyId) {
        // 1. Tìm bản sao sách
        BookCopy bookCopyToDelete = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_COPY_NOT_FOUND));

        // 2. KIỂM TRA QUY TẮC NGHIỆP VỤ (Vẫn rất quan trọng)
        if (bookCopyToDelete.getStatus() == BookCopyStatus.BORROWED) {
            throw new AppException(ErrorCode.CANNOT_DELETE_COPY_BORROWED);
        }

        if (bookCopyToDelete.getStatus() == BookCopyStatus.RESERVED) {
            throw new AppException(ErrorCode.CANNOT_DELETE_COPY_RESERVED);
        }

        // (Tùy chọn) Kiểm tra xem có ai đang giữ yêu cầu mượn không
        if (bookCopyToDelete.getPendingBorrow() != null) {
            throw new AppException(ErrorCode.CANNOT_DELETE_COPY_PENDING);
        }

        // 3. Nếu tất cả kiểm tra đều qua, thực hiện "xóa mềm"
        // Bằng cách cập nhật trạng thái của nó
        bookCopyToDelete.setStatus(BookCopyStatus.DISCARDED);
        bookCopyToDelete.setLocation("N/A"); // (Tùy chọn) Xóa vị trí

        bookCopyRepository.save(bookCopyToDelete);

        log.info("Successfully soft-deleted (discarded) book copy with ID: {}", copyId);
    }


    /**
     * Sinh ra một mã bản sao duy nhất.
     * Logic: ISBN + một chuỗi ngẫu nhiên/thời gian để đảm bảo tính duy nhất.
     * Cần có một vòng lặp để đảm bảo mã này chưa tồn tại trong CSDL.
     */
    private String generateUniqueCopyNumber(String isbn) {
        String uniquePart;
        String potentialCopyNumber;
        do {
            // Tạo một chuỗi ngẫu nhiên gồm 6 ký tự.
            // UUID.randomUUID().toString().substring(0, 6) là một cách đơn giản.
            uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            potentialCopyNumber = isbn + "-" + uniquePart;
        } while (bookCopyRepository.existsByCopyNumber(potentialCopyNumber)); // Kiểm tra sự tồn tại

        return potentialCopyNumber;
    }

    /**
     * Sinh ra mã QR.
     * Trong thực tế, mã QR thường là một URL hoặc một chuỗi JSON chứa thông tin.
     * Ở đây, chúng ta có thể đơn giản là dùng lại copyNumber hoặc tạo một UUID mới.
     */
    private String generateUniqueQrCode(String copyNumber) {
        // Cách an toàn nhất: Dùng một UUID hoàn toàn mới để tránh lộ thông tin
        String potentialQrCode;
        do {
            potentialQrCode = UUID.randomUUID().toString();
        } while (bookCopyRepository.existsByQrCode(potentialQrCode)); // Kiểm tra sự tồn tại

        return potentialQrCode;
    }
}
