package com.tuan.debtwizard.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String message;
    private T result;
    public static <T> ApiResponse<T> success(T result) {
        return ApiResponse.<T>builder()
                .message("Success")
                .result(result)
                .build();
    }

    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .message("Success")
                .build();
    }
}
