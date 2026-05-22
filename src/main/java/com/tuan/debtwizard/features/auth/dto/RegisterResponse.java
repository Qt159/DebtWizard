package com.tuan.debtwizard.features.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse {
    private Long id;
    private String username;
    private String fullName;
    public RegisterResponse(Long id, String username, String fullName) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
    }
    public RegisterResponse() {}
}