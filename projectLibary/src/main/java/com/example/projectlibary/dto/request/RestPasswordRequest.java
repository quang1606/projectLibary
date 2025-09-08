package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RestPasswordRequest {
    @NotBlank
    private String token;
    @NotBlank
    private String password;
}
