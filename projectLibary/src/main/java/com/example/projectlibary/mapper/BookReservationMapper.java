package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.BookReservationResponse;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.BookReservation;
import com.example.projectlibary.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookReservationMapper {
    public BookReservationResponse toResponse(BookReservation bookReservation) {
        if (bookReservation == null) {
            return null;
        }
        User user = bookReservation.getUser();
        Book book = bookReservation.getBook();
        return BookReservationResponse.builder()
                // Map các trường trực tiếp từ BookReservation
                .id(bookReservation.getId())
                .reservedAt(bookReservation.getReservedAt())
                .status(bookReservation.getStatus())
                .availableUntil(bookReservation.getAvailableUntil())
                .queuePosition(bookReservation.getQueuePosition())

                // Map các trường "làm phẳng" từ User, kiểm tra null cẩn thận
                .userId(user != null ? user.getId() : null)
                .userFullName(user != null ? user.getFullName() : "N/A")
                .userStudentId(user != null ? user.getStudentId() : null)

                // Map các trường "làm phẳng" từ Book, kiểm tra null cẩn thận
                .bookId(book != null ? book.getId() : null)
                .bookTitle(book != null ? book.getTitle() : "N/A")

                // Hoàn thành việc xây dựng đối tượng
                .build();


    }
    public List<BookReservationResponse> toResponseList(List<BookReservation> reservations) {
        if (reservations == null) {
            return List.of(); // Trả về danh sách rỗng để tránh NullPointerException
        }
        return reservations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
