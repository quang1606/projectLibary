package com.example.projectlibary.mapper;

import com.example.projectlibary.dto.reponse.AuthorResponse;
import com.example.projectlibary.model.Author;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AuthorMapper {
    public AuthorResponse toAuthorResponse(Author author) {
        if (author == null) {
            return null;
        }
        return AuthorResponse.builder()
                .id(author.getId())
                .bio(author.getBio())
                .name(author.getName())
                .avatar(author.getAvatar())
                .build();
    }
    public List<AuthorResponse> toAuthorResponseList(List<Author> authors) {
        if (authors == null) {
            return List.of();
        }
        return authors.stream().map(author -> toAuthorResponse(author)).collect(Collectors.toList());
    }
}
