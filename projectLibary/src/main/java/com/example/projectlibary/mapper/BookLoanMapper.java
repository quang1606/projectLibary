package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.BookLoanResponse;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookCopy;
import com.example.projectlibary.model.BookLoan;
import com.example.projectlibary.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookLoanMapper {

    public BookLoanResponse toResponse(BookLoan bookLoan) {
        if (bookLoan == null) {
            return null;
        }

        // --- Lấy ra các đối tượng liên quan để xử lý ---
        User user = bookLoan.getUser();
        BookCopy bookCopy = bookLoan.getBookCopy();
        // Lấy Book từ BookCopy, cần kiểm tra null cho bookCopy trước
        Book book = (bookCopy != null) ? bookCopy.getBook() : null;
        User librarian = bookLoan.getLibrarian();

        // --- Sử dụng builder để tạo đối tượng Response ---
        return BookLoanResponse.builder()
                // 1. Map các trường trực tiếp từ BookLoan
                .id(bookLoan.getId())
                .borrowedAt(bookLoan.getBorrowedAt())
                .dueDate(bookLoan.getDueDate())
                .librarianConfirmedReturnAt(bookLoan.getLibrarianConfirmedReturnAt())
                .fineAmount(bookLoan.getFineAmount())
                .status(bookLoan.getStatus())

                // 2. Map thông tin người mượn (User)
                .userId(user != null ? user.getId() : null)
                .userFullName(user != null ? user.getFullName() : "N/A")

                // 3. Map thông tin sách và bản sao (Book & BookCopy)
                .bookId(book != null ? book.getId() : null)
                .bookTitle(book != null ? book.getTitle() : "N/A")
                .bookCopyNumber(bookCopy != null ? bookCopy.getCopyNumber() : "N/A")

                // 4. Map thông tin thủ thư xử lý
                .librarianName(librarian != null ? librarian.getFullName() : "N/A")

                // Hoàn tất
                .build();
    }

    public List<BookLoanResponse> toResponseList(List<BookLoan> bookLoans) {
        if (bookLoans == null) {
            return List.of();
        }
        return bookLoans.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
