package com.example.projectlibary.controller.librairian;

import com.example.projectlibary.dto.reponse.AuthorResponse;
import com.example.projectlibary.dto.reponse.ResponseData;
import com.example.projectlibary.dto.request.CreateAuthorRequest;
import com.example.projectlibary.dto.request.UpdateAuthorRequest;
import com.example.projectlibary.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class LibrarianAuthorController {
    private final AuthorService authorService;
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') ")
    public ResponseEntity<ResponseData<AuthorResponse>> createAuthor(@Valid @RequestPart  CreateAuthorRequest request,
                                                                     @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {
        AuthorResponse authorResponse = authorService.createAuthor(request,avatarFile);
        ResponseData<AuthorResponse> responseData = new ResponseData<>(200,"Successfully created author",authorResponse);
        return ResponseEntity.ok(responseData);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') ")
    public ResponseEntity<ResponseData<AuthorResponse>> updateAuthor(@Valid @RequestPart UpdateAuthorRequest request,
                                                                     @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
                                                                     @PathVariable Long id) {
        AuthorResponse authorResponse = authorService.updateAuthor(request,avatarFile,id);
        ResponseData<AuthorResponse> responseData = new ResponseData<>(200,"Successfully created author",authorResponse);
        return ResponseEntity.ok(responseData);
    }
}
