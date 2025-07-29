package com.example.projectlibary.dto.request;

import com.example.projectlibary.common.ReturnCondition;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinalizeReturnRequest {
    @NotNull(message = "Loan ID cannot be null")
    private Long loanId;

    @NotNull(message = "Return condition must be specified")
    private ReturnCondition condition;

    private String notes; // Ghi chú (tùy chọn)
}
