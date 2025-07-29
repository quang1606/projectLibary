package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.BorrowingCartItemResponse;
import com.example.projectlibary.dto.reponse.BorrowingCartResponse;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookCopy;
import com.example.projectlibary.model.BorrowingCart;
import com.example.projectlibary.model.BorrowingCartItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BorrowingCartMapper {
    public BorrowingCartResponse toBorrowingCartResponse(BorrowingCart borrowingCart) {
        if (borrowingCart == null) {
            return null;
        }
        List<BorrowingCartItemResponse> cartItemResponses = borrowingCart.getItems()==null ? Collections.emptyList() :
                borrowingCart.getItems().stream().map(this::toCartItemResponse).toList();
        return BorrowingCartResponse.builder()
                .cartId(borrowingCart.getId())
                .userId(borrowingCart.getUser().getId())
                .status(borrowingCart.getStatus())
                .items(cartItemResponses)
                .totalItems(cartItemResponses.size())
                .expiresAt(borrowingCart.getExpiresAt())
                .confirmationCode(borrowingCart.getConfirmationCode())
                .confirmationCodeExpiresAt(borrowingCart.getConfirmationCodeExpiresAt())
                .build();

    }

    private BorrowingCartItemResponse toCartItemResponse(BorrowingCartItem borrowingCartItem) {
        if (borrowingCartItem == null) {
            return null;
        }
        BookCopy bookCopy = borrowingCartItem.getBookCopy();
        Book book = (bookCopy != null) ? bookCopy.getBook() : null;

        return BorrowingCartItemResponse.builder()
                .cartItemId(borrowingCartItem.getId())
                .addedAt(borrowingCartItem.getAddedAt())
                // Lấy thông tin từ BookCopy
                .bookCopyId((bookCopy != null) ? bookCopy.getId() : null)
                .copyNumber((bookCopy != null) ? bookCopy.getCopyNumber() : "N/A")
                // Lấy thông tin từ Book
                .bookId((book != null) ? book.getId() : null)
                .bookTitle((book != null) ? book.getTitle() : "N/A")
                .bookThumbnailUrl((book != null) ? book.getThumbnail() : null)
                .build();
    }
}
