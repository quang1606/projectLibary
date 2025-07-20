package com.example.projectlibary.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ToggleFavoriteResponse {
    private Long bookId;
    private boolean favourite;
}
