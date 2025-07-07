package com.example.projectlibary.mapper;

import com.example.projectlibary.common.BookCopyStatus;
import com.example.projectlibary.dto.reponse.BookDetailResponse;
import com.example.projectlibary.dto.reponse.BookSummaryResponse;
import com.example.projectlibary.model.Author;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookReview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class BookMapper {
    private final AuthorMapper authorMapper;
    private final CategoryMapper categoryMapper;

    @Autowired
    public BookMapper(AuthorMapper authorMapper, CategoryMapper categoryMapper) {
        this.authorMapper = authorMapper;
        this.categoryMapper = categoryMapper;
    }

    public double averageRating(Book book) {
        if (book.getReviews().isEmpty()){
            return 0.0;
        }
        return book.getReviews().stream().mapToInt(BookReview::getRating).average().orElse(0.0);
    }

    public BookDetailResponse toBookDetailResponse(Book book) {
        if (book == null) {
            return null;
        }

        // --- Tính toán các giá trị tổng hợp trước ---

        // 1. Tính số lượng bản sao có sẵn
        long availableCopies = (book.getBookCopies() == null) ? 0 : book.getBookCopies().stream()
                .filter(copy -> copy.getStatus() == BookCopyStatus.AVAILABLE)
                .count();
       long loanCount = book.getBookCopies().stream()
             .mapToLong(copy -> copy.getBookLoans().size()).sum();
        return BookDetailResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .isbn(book.getIsbn())
                .thumbnail(book.getThumbnail())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .replacementCost(book.getReplacementCost())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())

                // Sử dụng các mapper khác để map đối tượng lồng nhau

                .category(categoryMapper.toResponse(book.getCategory()))
                .authors(book.getAuthors()==null ? Collections.emptySet() :
                        book.getAuthors().stream().map(authorMapper::toAuthorResponse).collect(Collectors.toSet()))

                // Map các trường được làm phẳng (flattened)
                .createdByUsername(book.getCreatedBy() != null ? book.getCreatedBy().getUsername() : "N/A")
                .updatedByUsername(book.getUpdatedBy() != null ? book.getUpdatedBy().getUsername() : "N/A")

                // Gán các giá trị đã tính toán
                .availableCopies(availableCopies)
                .averageRating(averageRating(book))
                .loanCount(loanCount)
                .build();
    }
    public BookSummaryResponse toSummaryResponse(Book book) {
        if (book == null) {
            return null;
        }

        return BookSummaryResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .publicationYear(book.getPublicationYear())
                .thumbnail(book.getThumbnail())
                // Làm phẳng danh sách tên tác giả
                .authorNames(
                        book.getAuthors() == null ? Collections.emptySet() :
                                book.getAuthors().stream()
                                        .map(Author::getName) // Chỉ lấy tên tác giả
                                        .collect(Collectors.toSet())
                )

                // Làm phẳng tên thể loại
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : "N/A")
                .build();
    }
    public List<BookSummaryResponse> toSummaryResponseList(List<Book> books) {
        if (books == null) {
            return Collections.emptyList();
        }
        return books.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }
}
