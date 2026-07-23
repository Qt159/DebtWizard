package com.tuan.debtwizard.exception;

import com.tuan.debtwizard.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    // 1. Xử lý AppException
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.builder()
                        .code(errorCode.name())
                        .message(errorCode.getMessage())
                        .build());
    }

    // 2. Login thất bại
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.builder()
                        .code(ErrorCode.INVALID_CREDENTIALS.name())
                        .message(ErrorCode.INVALID_CREDENTIALS.getMessage())
                        .build());
    }

    // 3. Unauthorized access (Chưa đăng nhập hoặc token hết hạn/không hợp lệ)
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientAuthentication(InsufficientAuthenticationException ex) {
        logger.warn("Unauthorized access attempt: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.builder()
                        .code(ErrorCode.UNAUTHENTICATED.name())
                        .message(ErrorCode.UNAUTHENTICATED.getMessage())
                        .build());
    }

    // 4. Lỗi Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        logger.warn("Validation failed: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .code(ErrorCode.INVALID_INPUT.name())
                        .message("Dữ liệu không hợp lệ")
                        .result(errors)
                        .build());
    }
    // lỗi gửi ko đúng định dạng enum, hay kiểu dữ liệu
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        logger.warn("Invalid request body: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .code(ErrorCode.INVALID_INPUT.name())
                        .message("Dữ liệu gửi lên không đúng định dạng")
                        .build());
    }

    // 5. Lỗi hệ thống không xác định (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
        logger.error("Unexpected error occurred: ", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.builder()
                        .code(ErrorCode.INTERNAL_SERVER_ERROR.name())
                        .message(ErrorCode.INTERNAL_SERVER_ERROR.getMessage())
                        .build());
    }
}