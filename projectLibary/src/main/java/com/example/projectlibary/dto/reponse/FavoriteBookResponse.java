package com.example.projectlibary.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteBookResponse {

    private Long id; // ID của chính bản ghi "yêu thích"
    private Instant favoritedAt; // Thời điểm yêu thích

    // Thay vì trả về toàn bộ Entity Book, ta trả về một DTO tóm tắt của nó
    private BookSummaryResponse book;
}