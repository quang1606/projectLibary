package com.example.projectlibary.exception;

import com.example.projectlibary.dto.reponse.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalHandlingException {
    /**
     * Xử lý chính cho tất cả các lỗi nghiệp vụ được định nghĩa (AppException).
     * Đây là handler quan trọng nhất.
     */
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, WebRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        String path = request.getDescription(false).replace("uri=", "");
        // GHI LOG Ở ĐÂY
        log.warn("Business logic exception occurred : Code[{}] Message [{}] Path[{}]", errorCode.getCode(), errorCode.getMessage(),path);
        ErrorResponse errorResponse= ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .message(errorCode.getMessage())
                .path(path)

                .build();
        return ResponseEntity.status(errorCode.getHttpStatus().value()).body(errorResponse);
    }

    /**
     * Xử lý lỗi validation từ @Valid.
     * Trả về một cấu trúc lỗi chi tiết cho từng trường.
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        ErrorCode errorCode = ErrorCode.VALIDATION_EXCEPTION;
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, String> validationErrors  = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        log.warn("Validation failed: Path[{}], Errors[{}]", path, validationErrors);
        ErrorResponse errorResponse= ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .message(errorCode.getMessage())
                .path(path)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus().value()).body(errorResponse);
    }
    /**
     * Xử lý lỗi từ chối truy cập từ Spring Security.
     */
    @ExceptionHandler(value = org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        String path = request.getDescription(false).replace("uri=", "");
        log.warn("Access Denied: User attempted to access a forbidden resource. Path[{}]", path);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .message(errorCode.getMessage())
                .path(path)
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus().value()).body(errorResponse);
    }

    /**
     * Xử lý tất cả các lỗi không được định nghĩa khác (lưới cứu hộ).
     * Tránh để lộ chi tiết lỗi hệ thống ra ngoài.
     */
    public ResponseEntity<ErrorResponse> handleUncategorizedException(Exception ex, WebRequest request) {
        // Ghi lại log của lỗi thực tế để đội ngũ phát triển có thể debug
        log.error("Uncategorized exception occurred: ", ex);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .message("An unexpected error occurred. Please contact support.") // Trả về message an toàn
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(errorResponse);
    }

}
