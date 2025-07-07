package com.example.projectlibary.dto.reponse;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.example.projectlibary.model.Category}
 */

@Builder
@Value
public class CategoryResponse implements Serializable {
    Long id;
    String name;
}