package com.tuan.debtwizard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tuan.debtwizard.exception.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // Chỉ render những trường không null ra JSON
public class ErrorResponse {

    private final String code;
    private final String message;
    private final int status;

    // Map chứa lỗi chi tiết (ví dụ: "username": "Không được để trống")
    private Map<String, String> details;

    // 1. Constructor dùng với ErrorCode Enum
    public ErrorResponse(ErrorCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }

    // 2. Constructor dùng cho lỗi Validation (Có thêm map details)
    public ErrorResponse(ErrorCode errorCode, Map<String, String> details) {
        this(errorCode);
        this.details = details;
    }

    // 3. Constructor linh hoạt (Dùng cho các lỗi từ Filter hoặc Exception lạ)
    public ErrorResponse(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}