package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmLoanRequest {
    @NotBlank
    @Size(min = 6, max = 6)
    private String confirmationCode;
}
