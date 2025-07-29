package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemToCartRequest {
    @NotBlank
    private String qrCode;
}
