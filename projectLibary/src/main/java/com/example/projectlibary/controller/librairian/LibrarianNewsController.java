package com.example.projectlibary.controller.librairian;

import com.example.projectlibary.dto.reponse.NewsDetailResponse;
import com.example.projectlibary.dto.reponse.NewsSummaryResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.request.CreateNewsRequest;
import com.example.projectlibary.dto.request.UpdateNewsRequest;
import com.example.projectlibary.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class LibrarianNewsController {
    private final NewsService newsService;
    @PostMapping("")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<ResponseData<NewsDetailResponse>> createPost(@Valid @RequestBody CreateNewsRequest createNewsRequest) {
        NewsDetailResponse detailResponse = newsService.createNews(createNewsRequest);
        ResponseData<NewsDetailResponse> responseData =new ResponseData<>(200,"Success",detailResponse);
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/getAll")
    public ResponseEntity<ResponseData<PageResponse<NewsSummaryResponse>>> getAllPost(@RequestParam(defaultValue = "0") int page,
                                                                                      @RequestParam(defaultValue = "18") int size){
        PageResponse<NewsSummaryResponse> pageResponse = newsService.getAllNew(page,size);
        ResponseData<PageResponse<NewsSummaryResponse>> responseData = new ResponseData<>(200,"Success",pageResponse);
        return ResponseEntity.ok(responseData);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<NewsDetailResponse>> getPostById(@PathVariable("id") long id){
        NewsDetailResponse newById = newsService.getNewById(id);
        ResponseData<NewsDetailResponse> responseData = new ResponseData<>(200,"Success",newById);
        return ResponseEntity.ok(responseData);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<ResponseData<NewsDetailResponse>> updatePost(@Valid @RequestBody UpdateNewsRequest updateNewsRequest, @PathVariable long id) {
        NewsDetailResponse detailResponse = newsService.updateNews(updateNewsRequest,id);
        ResponseData<NewsDetailResponse> responseData =new ResponseData<>(200,"Success",detailResponse);
        return ResponseEntity.ok(responseData);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deletePost(@RequestParam long id) {
        newsService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

}
