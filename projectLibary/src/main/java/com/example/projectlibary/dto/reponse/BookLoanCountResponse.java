package com.example.projectlibary.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BookLoanCountResponse {
    Long id;
    String title;
    Long loanCount;
}
