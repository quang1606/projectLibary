package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.BookSummaryResponse;
import com.example.projectlibary.dto.reponse.FavouriteBookResponse;
import com.example.projectlibary.model.UserFavorite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
@Component
public class FavoriteMapper {
    private final BookMapper bookMapper;

    @Autowired
    public FavoriteMapper(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public FavouriteBookResponse toFavoriteBookResponse(UserFavorite entity) {
        if (entity == null) {
            return null;
        }
        // Lấy thông tin tóm tắt của sách từ entity
        BookSummaryResponse bookSummary = bookMapper.toSummaryResponse(entity.getBook());

        // Tạo đối tượng response chính
        return new FavouriteBookResponse(
                entity.getId(),
                entity.getFavoritedAt(),
                bookSummary

        );
    }

    // Bạn cũng có thể tạo một phương thức để chuyển đổi cả một danh sách
    public  List<FavouriteBookResponse> toFavoriteBookResponseList(List<UserFavorite> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toFavoriteBookResponse)
                .collect(Collectors.toList());
    }
}

