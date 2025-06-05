package com.example.projectlibary.dto.reponse;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.example.projectlibary.model.Book}
 */
@Value
@Builder
public class BookSummaryResponse implements Serializable {
    Long id;
    String title;
    String isbn;
}