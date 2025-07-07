package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.BookReviewResponse;
import com.example.projectlibary.model.BookReview;
import com.example.projectlibary.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookReviewMapper {
    public BookReviewResponse toBookReviewResponse(BookReview bookReview) {
        if (bookReview == null) return null;

        // Lấy ra đối tượng User liên quan để tái sử dụng và kiểm tra null
        User user = bookReview.getUser();
        return BookReviewResponse.builder()
                .id(bookReview.getId())
                .rating(bookReview.getRating())
                .comment(bookReview.getComment())
                .reviewDate(bookReview.getReviewDate())

                // Map các trường "làm phẳng" từ User, kiểm tra null cẩn thận
                .userId(user!=null?user.getId():null)
                .userFullName(user!=null? user.getFullName() : null)
                .userAvatar(user!=null? user.getAvatar() : null)
                .build();
    }
    public List<BookReviewResponse> toBookReviewResponseList(List<BookReview> bookReviews) {
        if (bookReviews == null) return Collections.emptyList();
        return bookReviews.stream().map(this::toBookReviewResponse).collect(Collectors.toList());

    }
}
