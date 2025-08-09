package com.example.projectlibary.service.implement;

import com.example.projectlibary.dto.reponse.AuthorResponse;
import com.example.projectlibary.dto.reponse.PageResponse;
import com.example.projectlibary.dto.request.CreateAuthorRequest;
import com.example.projectlibary.dto.request.UpdateAuthorRequest;
import com.example.projectlibary.exception.AppException;
import com.example.projectlibary.exception.ErrorCode;
import com.example.projectlibary.mapper.AuthorMapper;
import com.example.projectlibary.model.Author;
import com.example.projectlibary.repository.AuthorRepository;
import com.example.projectlibary.service.AuthorService;
import com.example.projectlibary.service.BookService;
import com.example.projectlibary.service.CloudinaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorServiceImplement implements AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final CloudinaryService cloudinaryService;
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

    @Override
    @Transactional
    public AuthorResponse createAuthor(CreateAuthorRequest request, MultipartFile avatarFile) {
        if(authorRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.AUTHOR_ALREADY_EXISTS);
        }
        String avatarUrl = null;
        if(avatarFile != null && !avatarFile.isEmpty()) {
            try {
                Map result = cloudinaryService.uploadFile(avatarFile, "library/avatars");
                avatarUrl = result.get("url").toString();
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }
        Author newAuthor = Author.builder()
                .name(request.getName())
                .bio(request.getBio())
                .avatar(avatarUrl)
                .build();

        Author savedAuthor = authorRepository.save(newAuthor);
        return authorMapper.toAuthorResponse(savedAuthor);

    }

    @Override
    @Transactional
    public AuthorResponse updateAuthor(UpdateAuthorRequest request, MultipartFile avatarFile, Long id) {
        Author authorToUpdate = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_FOUND));


        if (request.getName() != null && !request.getName().isBlank()) {
            authorToUpdate.setName(request.getName());
        }
        if (request.getBio() != null) {
            authorToUpdate.setBio(request.getBio());
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                Map result = cloudinaryService.uploadFile(avatarFile, "library/avatars");
                authorToUpdate.setAvatar(result.get("url").toString());
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
            }
        }

        Author updatedAuthor = authorRepository.save(authorToUpdate);
        return authorMapper.toAuthorResponse(updatedAuthor);

    }


}
