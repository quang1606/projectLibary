package com.example.projectlibary.dto.reponse;

import lombok.*;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link com.example.projectlibary.model.Author}
 */
@Builder
@Value
public class AuthorDto implements Serializable {
    Long id;
    String name;
    String bio;
    private Set<BookSummaryResponse> books; // Thông tin tóm tắt về sách
}