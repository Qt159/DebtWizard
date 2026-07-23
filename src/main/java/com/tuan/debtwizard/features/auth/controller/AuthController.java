package com.tuan.debtwizard.features.auth.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.auth.dto.LoginRequest;
import com.tuan.debtwizard.features.auth.dto.LoginResponse;
import com.tuan.debtwizard.features.auth.dto.RegisterRequest;
import com.tuan.debtwizard.features.auth.dto.RegisterResponse;
import com.tuan.debtwizard.features.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ApiResponse.success(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {
        return ApiResponse.success(authService.login(loginRequest));
    }
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody String refreshToken) {
        return ApiResponse.success(authService.refresh(refreshToken));
    }
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
         if (userDetails == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
        authService.logout(userDetails.getUsername());
        return ApiResponse.success();
    }

}