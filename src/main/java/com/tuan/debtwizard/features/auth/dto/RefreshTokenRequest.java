package com.tuan.debtwizard.features.auth.dto;

import lombok.Setter;
import lombok.Getter;
import jakarta.validation.constraints.NotBlank;
@Getter
@Setter
public class RefreshTokenRequest{
    @NotBlank(message = "Refresh token không được để trống")
    private String RefreshToken;
}