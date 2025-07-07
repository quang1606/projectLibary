package com.example.projectlibary.dto.reponse;

import lombok.*;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link com.example.projectlibary.model.Author}
 */
@Builder
@Value
public class AuthorResponse implements Serializable {
    Long id;
    String name;
    String bio;
    String avatar;
}