package com.tuan.debtwizard.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        // Truyền message từ ErrorCode lên lớp cha RuntimeException
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}