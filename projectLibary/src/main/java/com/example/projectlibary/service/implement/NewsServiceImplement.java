package com.example.projectlibary.service.implement;

import com.example.projectlibary.dto.reponse.NewsDetailResponse;
import com.example.projectlibary.dto.reponse.NewsSummaryResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateNewsRequest;
import com.example.projectlibary.dto.request.UpdateNewsRequest;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.NewsMapper;
import com.example.projectlibary.model.News;
import com.example.projectlibary.model.User;
import com.example.projectlibary.repository.NewsRepository;
import com.example.projectlibary.repository.UserRepository;
import com.example.projectlibary.service.NewsService;
import com.example.projectlibary.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class NewsServiceImplement implements NewsService {
    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;
    private final UserRepository userRepository;
    @Override
    public PageResponse<NewsSummaryResponse> getAllNew(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsRepository.findAll(pageable);
        List<NewsSummaryResponse> list = newsMapper.toSummaryResponseList(newsPage.getContent());

        return PageResponse.from(newsPage,list);
    }

    @Override
    public NewsDetailResponse getNewById(long id) {
        News newsDetailResponse = newsRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        return newsMapper.toDetailResponse(newsDetailResponse);
    }

    @Override
    @Transactional
    public NewsDetailResponse createNews(CreateNewsRequest createNewsRequest) {
        News newPost = News.builder()
                .title(createNewsRequest.getTitle())
                .content(sanitizeHtml(createNewsRequest.getContent()))
                .status(createNewsRequest.getStatus())
                .build();
        // KHI GỌI SAVE, Spring SẼ TỰ ĐỘNG GỌI auditorProvider()
        // VÀ ĐIỀN VÀO createdBy, createdAt
        News savedPost = newsRepository.save(newPost);

        return newsMapper.toDetailResponse(savedPost);
    }

    @Override
    @Transactional
    public NewsDetailResponse updateNews(UpdateNewsRequest updateNewsRequest, long id) {
        News newsDetailResponse = newsRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.POST_NOT_FOUND));
        if(updateNewsRequest.getTitle()!=null && !updateNewsRequest.getTitle().isBlank()) {
            newsDetailResponse.setTitle(updateNewsRequest.getTitle());
        }
        if (updateNewsRequest.getContent() != null) {
            String sanitizedContent = sanitizeHtml(updateNewsRequest.getContent());
            newsDetailResponse.setContent(sanitizedContent);
        }
        if (updateNewsRequest.getStatus() != null) {
            newsDetailResponse.setStatus(updateNewsRequest.getStatus());
        }
        News updatedPost = newsRepository.save(newsDetailResponse);
        return newsMapper.toDetailResponse(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(long id) {
        if (!newsRepository.existsById(id)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
        newsRepository.deleteById(id);
    }

    private String sanitizeHtml(String untrustedHtml) {
        // Ví dụ sử dụng JSoup
        // Cần thêm dependency: org.jsoup:jsoup
        return Jsoup.clean(untrustedHtml, Safelist.basicWithImages());
    }
}
