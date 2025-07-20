package com.example.projectlibary.service.implement;

import com.example.projectlibary.dto.reponse.FavouriteBookResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.ToggleFavoriteResponse;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.FavoriteMapper;
import com.example.projectlibary.model.Book;
import com.example.projectlibary.model.CustomUserDetails;
import com.example.projectlibary.model.User;
import com.example.projectlibary.model.UserFavorite;
import com.example.projectlibary.repository.BookRepository;
import com.example.projectlibary.repository.UserFavoriteRepository;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.service.FavouriteService;
import com.example.projectlibary.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class favouriteServiceImplement implements FavouriteService {
private final UserFavoriteRepository userFavoriteRepository;
private final UserRepository userRepository;
private final FavoriteMapper favoriteMapper;
private final BookRepository bookRepository;
    @Override
    public PageResponse<FavouriteBookResponse> getAllFavourite(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        Page<UserFavorite> favoriteList = userFavoriteRepository.findByUser_Id( userDetails.getId(), pageable);
        List<FavouriteBookResponse> favouriteBookResponseList = favoriteMapper.toFavoriteBookResponseList(favoriteList.getContent());
        return PageResponse.from(favoriteList,favouriteBookResponseList);
    }


    @Override
    @Transactional
    public void deleteFavouriteAllBook() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        userFavoriteRepository.deleteByUser_Id(userDetails.getId());
    }

    public boolean isFavourite(Long id) { // 'id' ở đây là bookId
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userFavoriteRepository.existsByUser_IdAndBook_Id(userDetails.getId(), id);
    }

    @Override
    public ToggleFavoriteResponse toggleFavoriteBook(Long bookId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findById(bookId).orElseThrow(()->new AppException(ErrorCode.BOOK_NOT_FOUND));
        Optional<UserFavorite> userFavorite = userFavoriteRepository.findByBook_IdAndUser_Id(bookId,user.getId());
        if(userFavorite.isPresent()){
            userFavoriteRepository.delete(userFavorite.get());
            return new ToggleFavoriteResponse(bookId, false);
        }else {
            UserFavorite newFavorite = UserFavorite.builder()
                    .book(book)
                    .user(user)
                    .build();
            userFavoriteRepository.save(newFavorite);
            return new ToggleFavoriteResponse(bookId, true);
        }
    }


}
