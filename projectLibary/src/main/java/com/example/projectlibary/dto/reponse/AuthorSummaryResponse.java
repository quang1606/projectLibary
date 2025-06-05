package com.example.projectlibary.dto.reponse;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.example.projectlibary.model.Author}
 */
@Builder
@Value
public class AuthorSummaryResponse implements Serializable {
    Long id;
    String name;
    String bio;
}