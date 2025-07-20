package com.example.projectlibary.service.implement;

import com.example.projectlibary.dto.reponse.BookReviewResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateReviewRequest;
import com.example.projectlibary.dto.request.UpdateReviewRequest;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.BookReviewMapper;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookReview;
import com.example.projectlibary.model.User;
import com.example.projectlibary.repository.BookRepository;
import com.example.projectlibary.repository.BookReviewRepository;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.service.BookReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookReviewServiceImplement implements BookReviewService {
    private final BookReviewRepository bookReviewRepository;
    private final BookReviewMapper bookReviewMapper;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    @Override
    public PageResponse<BookReviewResponse> getAllReview(int page, int size, Long id) {
        if(!bookRepository.existsById(id)) {
            throw new AppException(ErrorCode.BOOK_NOT_FOUND);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<BookReview> bookReviews = bookReviewRepository.findByBookId(id,pageable);
        List<BookReviewResponse> bookReviewResponseList = bookReviewMapper.toBookReviewResponseList(bookReviews.getContent());
        return PageResponse.from(bookReviews,bookReviewResponseList);
    }

    @Override
    @Transactional
    public BookReviewResponse createReview(CreateReviewRequest createReviewRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElseThrow(()->new  AppException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findById(createReviewRequest.getBookId()).orElseThrow(()->new  AppException(ErrorCode.BOOK_NOT_FOUND));
        if(bookReviewRepository.existsByUserAndBook(user,book)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        BookReview bookReview =  BookReview.builder()
                .comment(createReviewRequest.getComment())
                .rating(createReviewRequest.getRating())
                .book(book)
                .user(user)
                .build();
        bookReviewRepository.save(bookReview);
        return bookReviewMapper.toBookReviewResponse(bookReview);
    }

    @Override
    public BookReviewResponse updateReview(UpdateReviewRequest updateReviewRequest, Long id) {
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        BookReview bookReview = bookReviewRepository.findById(id).orElseThrow(()->new  AppException(ErrorCode.REVIEW_NOT_FOUND));
        if(!bookReview.getUser().getEmail().equals(username)) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        bookReview.setComment(updateReviewRequest.getComment());
        bookReview.setRating(updateReviewRequest.getRating());
        bookReviewRepository.save(bookReview);
        return bookReviewMapper.toBookReviewResponse(bookReview);
    }

    @Override
    public void deleteReview(Long id) {
        Authentication authentication =SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        BookReview bookReview = bookReviewRepository.findById(id).orElseThrow(()->new  AppException(ErrorCode.REVIEW_NOT_FOUND));
        if(!bookReview.getUser().getEmail().equals(username)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        bookReviewRepository.delete(bookReview);
    }


}
