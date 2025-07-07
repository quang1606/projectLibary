package com.example.projectlibary.service.implement;

import com.example.projectlibary.dto.reponse.AuthorResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.AuthorMapper;
import com.example.projectlibary.model.Author;
import com.example.projectlibary.repository.AuthorRepository;
import com.example.projectlibary.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorServiceImplement implements AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    @Override
    public PageResponse<AuthorResponse> getAllAuthor(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Author> authors = authorRepository.findAll(pageable);
        List<AuthorResponse> authorResponses = authorMapper.toAuthorResponseList(authors.getContent());
        return PageResponse.from(authors,authorResponses);
    }

    @Override
    public AuthorResponse getAuthorById(long id) {

      Author author = authorRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_FOUND));
        return authorMapper.toAuthorResponse(author);
    }
}
