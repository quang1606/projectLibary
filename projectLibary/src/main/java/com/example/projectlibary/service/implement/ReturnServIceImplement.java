package com.example.projectlibary.service.implement;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.common.BookLoanStatus;
import com.example.projectlibary.common.ReturnCondition;
import com.example.projectlibary.dto.reponse.BookLoanResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.FinalizeReturnRequest;
import com.example.projectlibary.event.BookStatusChangedEvent;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.BookLoanMapper;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookCopy;
import com.example.projectlibary.model.BookLoan;
import com.example.projectlibary.model.CustomUserDetails;
import com.example.projectlibary.repository.BookCopyRepository;
import com.example.projectlibary.repository.BookLoanRepository;
import com.example.projectlibary.repository.BookRepository;
import com.example.projectlibary.service.FineService;
import com.example.projectlibary.service.NotificationService;
import com.example.projectlibary.service.ReturnService;
import com.example.projectlibary.service.eventservice.KafkaProducerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnServIceImplement implements ReturnService{
    private final BookLoanRepository bookLoanRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookLoanMapper bookLoanMapper;
    private final BookRepository bookRepository;
    private final NotificationService notificationService;
    private final KafkaProducerService kafkaProducerService;
    private final FineService fineService;
    private static final int PENDING_RETURN_EXPIRATION_HOURS = 12;
    private static final int PENDING_RETURN_EXPIRATION_DAYS = 1;

    @Scheduled(fixedRate = 360000)
    @Transactional
    public void cancelExpiredPendingReturns(){
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(PENDING_RETURN_EXPIRATION_HOURS);

        List<BookLoan> bookLoans = bookLoanRepository.findByStatusAndStudentInitiatedReturnAtBefore(BookLoanStatus.PENDING_RETURN, expirationTime);
        if(bookLoans.isEmpty()){
            return;
        }

        for(BookLoan bookLoan : bookLoans){
            bookLoan.setStatus(BookLoanStatus.ON_LOAN);
            bookLoan.setStudentInitiatedReturnAt(null);
        }
    }

    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void checkBooksExpiringInOneDay() {
        LocalDate dateTime = LocalDate.now().minusDays(PENDING_RETURN_EXPIRATION_DAYS);
        List<BookLoan> bookLoans = bookLoanRepository.findByStatusAndDueDateAndReminderSentFalse(BookLoanStatus.ON_LOAN, dateTime);
        if(bookLoans.isEmpty()){
            return;
        }
        for(BookLoan bookLoan : bookLoans){
            notificationService.createAlertNotification(bookLoan);
            bookLoan.setReminderSent(true);
        }
    }


    @Override
    public PageResponse<BookLoanResponse> getMyLoans(int page, int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Pageable pageable = PageRequest.of(page,size);
        List<BookLoanStatus> loanStatuses = List.of(BookLoanStatus.ON_LOAN,BookLoanStatus.OVERDUE);
       Page<BookLoan> bookLoan =  bookLoanRepository.findByUser_IdAndStatusIn( userDetails.getId(),loanStatuses,pageable);
       if(bookLoan.isEmpty()){
           throw new AppException(ErrorCode.BOOK_LOAN_NOT_FOUND);
       }

        List<BookLoanResponse> loanResponses = bookLoanMapper.toResponseList(bookLoan.getContent());

        return PageResponse.from(bookLoan,loanResponses);
    }

    @Override
    @Transactional
    public BookLoanResponse returnsBook(Long loanId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        BookLoan bookLoan = bookLoanRepository.findById(loanId)
                .orElseThrow(()-> new AppException(ErrorCode.BOOK_LOAN_NOT_FOUND));
        if(!bookLoan.getUser().getId().equals(userDetails.getId())){
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        if(!bookLoan.getStatus().equals(BookLoanStatus.ON_LOAN) && !bookLoan.getStatus().equals(BookLoanStatus.OVERDUE)){
            throw new AppException(ErrorCode.BOOK_LOAN_NOT_FOUND);
        }
        bookLoan.setStatus(BookLoanStatus.PENDING_RETURN);
        bookLoan.setStudentInitiatedReturnAt(LocalDateTime.now());
        return bookLoanMapper.toResponse(bookLoan);
    }

    @Override
    @Transactional
    public BookLoanResponse getBookLoanByCopy(long id) {
        BookCopy bookCopy = bookCopyRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.BOOK_COPY_NOT_FOUND));
        BookLoan bookLoan = bookLoanRepository.findByBookCopyAndStatus(bookCopy, BookLoanStatus.PENDING_RETURN)
                .orElseThrow(()-> new AppException(ErrorCode.INVALID_RETURN_STATE));
        bookLoan.setStudentInitiatedReturnAt(LocalDateTime.now());

        return bookLoanMapper.toResponse(bookLoan);
    }

    @Override
    @Transactional
    public BookLoanResponse finalizeReturn(FinalizeReturnRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        BookLoan bookLoan = bookLoanRepository.findById(request.getLoanId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_LOAN_NOT_FOUND));
        if (bookLoan.getStatus() != BookLoanStatus.PENDING_RETURN) {
            throw new AppException(ErrorCode.INVALID_RETURN_STATE);
        }
        // 3. Lấy bản sao sách liên quan
        BookCopy bookCopy = bookLoan.getBookCopy();
        Book book = bookRepository.findById(bookCopy.getBook().getId())
                .orElseThrow(()-> new AppException(ErrorCode.BOOK_NOT_FOUND));
        BigDecimal bigDecimal = book.getReplacementCost();
        ReturnCondition condition = request.getCondition();
        // 4. Cập nhật thông tin từ request của thủ thư
        bookLoan.setLibrarianConfirmedReturnAt(LocalDateTime.now());
        bookLoan.setReturnCondition(request.getCondition());
        bookLoan.setReturnNotes(request.getNotes());

        fineService.createFineForLoan(bookLoan, condition, bigDecimal);
        switch (request.getCondition()) {
            case NORMAL:
                bookLoan.setStatus(BookLoanStatus.RETURNED);
                bookCopy.setStatus(BookCopyStatus.AVAILABLE);
                notificationService.createReturnSuccessNotification(bookLoan);
                break;
            case SLIGHTLY_DAMAGED:
                bookLoan.setStatus(BookLoanStatus.RETURNED);
                bookCopy.setStatus(BookCopyStatus.DAMAGED); // Sách cần được sửa chữa, không thể cho mượn ngay
                notificationService.createReturnSlightlyDamagedNotification(bookLoan,bigDecimal);
                break;
            case HEAVILY_DAMAGED:
                bookLoan.setStatus(BookLoanStatus.RETURNED);
                bookCopy.setStatus(BookCopyStatus.DAMAGED);
                notificationService.createReturnLostOrHeavilyDamagedNotification(bookLoan,bigDecimal);
                break;
            case LOST:
                bookLoan.setStatus(BookLoanStatus.LOST); // Trạng thái cuối cùng của lượt mượn là "Mất"
                bookCopy.setStatus(BookCopyStatus.LOST); // Trạng thái cuối cùng của bản sao là "Mất"
                notificationService.createReturnLostOrHeavilyDamagedNotification(bookLoan,bigDecimal);
                break;
        }
        handleBookStatusChange(userDetails.getId(),book.getId());

        bookLoanRepository.save(bookLoan);
        bookCopyRepository.save(bookCopy);
        return bookLoanMapper.toResponse(bookLoan);
    }

    @Override
    @Transactional
    public BookLoanResponse lostBook(Long loanId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        BookLoan bookLoan = bookLoanRepository.findById(loanId)
                .orElseThrow(()-> new AppException(ErrorCode.BOOK_LOAN_NOT_FOUND));
        BookCopy bookCopy = bookLoan.getBookCopy();
        if(bookLoan.getStatus() != BookLoanStatus.PENDING_RETURN || bookLoan.getReturnCondition() != ReturnCondition.NORMAL){
            throw new AppException(ErrorCode.INVALID_RETURN_STATE);
        }
        Book book = bookRepository.findById(bookCopy.getBook().getId()).orElseThrow(()-> new AppException(ErrorCode.BOOK_NOT_FOUND));
        BigDecimal bigDecimal = book.getReplacementCost();
        bookLoan.setStatus(BookLoanStatus.LOST);
        bookCopy.setStatus(BookCopyStatus.LOST);
        notificationService.createReturnLostOrHeavilyDamagedNotification(bookLoan,bigDecimal);
        handleBookStatusChange(userDetails.getId(),book.getId());
        bookLoanRepository.save(bookLoan);
        return bookLoanMapper.toResponse(bookLoan);
    }

    private void handleBookStatusChange(Long userId, Long bookId) {
        BookStatusChangedEvent event = BookStatusChangedEvent.builder()
                .userId(userId)
                .bookId(bookId)
                .addedAt(LocalDateTime.now())
                .build();
        kafkaProducerService.sendBookCopyId(event);
    }

}


