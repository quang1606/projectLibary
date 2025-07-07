package com.example.projectlibary.dto.reponse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public class PageResponse<T> implements Serializable {
        private final int pageNumber;
        private final int pageSize;
        private final int totalPages;
        private final Long totalElements;
        private final List<T> data;
        private final boolean first;
        private final boolean last;

        public static <T> PageResponse<T> from(Page<?> springPage,List<T> data) {
            return PageResponse.<T>builder()
                    .pageNumber(springPage.getNumber())
                    .pageSize(springPage.getSize())
                    .totalPages(springPage.getTotalPages())
                    .totalElements(springPage.getTotalElements())
                    .data(data)
                    .first(springPage.isFirst())
                    .last(springPage.isLast())
                    .build();
        }

        public static <T> PageResponse<T> empty(int pageNumber, int pageSize) {
            return PageResponse.<T>builder()
                    .pageNumber(pageNumber)
                    .pageSize(pageSize)
                    .totalPages(0)
                    .totalElements(0L)
                    .data(null)
                    .first(false)
                    .last(false)
                    .build();
        }
    }
