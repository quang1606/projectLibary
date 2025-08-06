package com.example.projectlibary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateTransactionRequest {
    @NotEmpty
    private List<Long> transactionIds;
}
