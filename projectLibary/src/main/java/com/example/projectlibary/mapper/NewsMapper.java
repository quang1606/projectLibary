package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.NewsDetailResponse;
import com.example.projectlibary.dto.reponse.NewsSummaryResponse;
import com.example.projectlibary.model.News;
import com.example.projectlibary.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NewsMapper {
    public NewsDetailResponse toDetailResponse(News news) {
        if (news == null) {
            return null;
        }

        User createdByUser = news.getCreatedBy();
        User updatedByUser = news.getUpdatedBy();

        return NewsDetailResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent()) // Bao gồm cả nội dung chi tiết
                .status(news.getStatus())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .createdByFullName(createdByUser != null ? createdByUser.getFullName() : "N/A")
                .updatedByFullName(updatedByUser != null ? updatedByUser.getFullName() : "N/A")
                .build();
    }


    public NewsSummaryResponse toSummaryResponse(News news) {
        if (news == null) {
            return null;
        }

        User createdByUser = news.getCreatedBy();

        return NewsSummaryResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .status(news.getStatus())
                .createdAt(news.getCreatedAt())
                .createdByFullName(createdByUser != null ? createdByUser.getFullName() : "N/A")
                .build();
    }


    public List<NewsSummaryResponse> toSummaryResponseList(List<News> newsList) {
        if (newsList == null) {
            return Collections.emptyList();
        }
        return newsList.stream()
                .map(this::toSummaryResponse) // Tái sử dụng logic của toSummaryResponse
                .collect(Collectors.toList());
    }
}
