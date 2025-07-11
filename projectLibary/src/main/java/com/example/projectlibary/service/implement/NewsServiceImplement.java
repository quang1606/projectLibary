package com.example.projectlibary.service.implement;

import com.example.projectlibary.dto.reponse.NewsDetailResponse;
import com.example.projectlibary.dto.reponse.NewsSummaryResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.NewsMapper;
import com.example.projectlibary.model.News;
import com.example.projectlibary.repository.NewsRepository;
import com.example.projectlibary.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class NewsServiceImplement implements NewsService {
    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;
    @Override
    public PageResponse<NewsSummaryResponse> getAllNew(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<News> newsPage = newsRepository.findAll(pageable);
        List<NewsSummaryResponse> list = newsMapper.toSummaryResponseList(newsPage.getContent());

        return PageResponse.from(newsPage,list);
    }

    @Override
    public NewsDetailResponse getNewById(long id) {
        News newsDetailResponse = newsRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.POSR_NOT_FOUND));
        return newsMapper.toDetailResponse(newsDetailResponse);
    }
}
