package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCartItemRequest {
    // ĐÚNG: Dùng @NotBlank cho String
    @NotBlank(message = "The scanned QR code data cannot be blank.")
        private String qrCode;
}
