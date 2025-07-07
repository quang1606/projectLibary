package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.BookCopyResponse;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookCopy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookCopyMapper {
    public BookCopyResponse toBookCopyResponse( BookCopy bookCopy ) {
        if (bookCopy == null) {
            return null;
        }
        Book book = bookCopy.getBook();
      return   BookCopyResponse.builder()
                .id(bookCopy.getId())
                .copyNumber(bookCopy.getCopyNumber())
                .qrCode(bookCopy.getQrCode())
                .status(bookCopy.getStatus())
                .location(bookCopy.getLocation())
                .addedDate(bookCopy.getAddedDate())
                // Map các trường "làm phẳng" từ Book, kiểm tra null cẩn thận
                .bookId(book != null ? book.getId() : null)
                .bookTitle(book != null ? book.getTitle() : null)
                .bookIsbn(book != null ? book.getIsbn() : null)

                .build();

    }
    public List<BookCopyResponse> toBookCopyResponseList(List<BookCopy> bookCopyList ) {
        if (bookCopyList == null) {
            return List.of();
        }
        return bookCopyList.stream().map(this::toBookCopyResponse).collect(Collectors.toList());
    }
}
