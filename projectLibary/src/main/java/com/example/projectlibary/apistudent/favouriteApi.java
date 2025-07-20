package com.example.projectlibary.apistudent;

import com.example.projectlibary.dto.reponse.FavouriteBookResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.reponse.ToggleFavoriteResponse;
import com.example.projectlibary.service.FavouriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/favourite")
@RequiredArgsConstructor
public class favouriteApi {
    private final FavouriteService favouriteService;
    @GetMapping("/get")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResponseData<PageResponse<FavouriteBookResponse>>> getAllFavoriteBooks(@RequestParam(value = "0") int page,
                                                                                                 @RequestParam(value = "12") int size) {
        PageResponse<FavouriteBookResponse> favoriteBookResponse =favouriteService.getAllFavourite(page, size);
        ResponseData<PageResponse<FavouriteBookResponse>> responseData = new ResponseData<>(200, "Success", favoriteBookResponse);
        return ResponseEntity.ok(responseData);
    }
    @PostMapping("/toggle/{bookId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResponseData<ToggleFavoriteResponse>> toggleFavorite(@PathVariable Long bookId) {
        ToggleFavoriteResponse response = favouriteService.toggleFavoriteBook(bookId);
        ResponseData<ToggleFavoriteResponse> responseData = new ResponseData<>(200, "Success", response);
        return ResponseEntity.ok(responseData);
    }
    @DeleteMapping("/delete_all")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> deleteAllFavouriteBooks(){
        favouriteService.deleteFavouriteAllBook();
        return ResponseEntity.ok().build();
    }
}
