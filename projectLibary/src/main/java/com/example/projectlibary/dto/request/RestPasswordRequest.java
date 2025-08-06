package com.example.projectlibary.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RestPasswordRequest {
    private String password;
}
