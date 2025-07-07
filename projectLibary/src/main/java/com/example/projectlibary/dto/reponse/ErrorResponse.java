package com.example.projectlibary.dto.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse implements Serializable {
    private LocalDateTime timestamp;
    private int status;
    private int code;
    private String message;
    private String error;
    private String path;
    private Map<String, String> validationErrors;
}
