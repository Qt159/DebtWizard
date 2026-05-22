package com.tuan.debtwizard.features.auth.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.auth.dto.UserResponse;
import com.tuan.debtwizard.features.auth.dto.LoginRequest;
import com.tuan.debtwizard.features.auth.dto.LoginResponse;
import com.tuan.debtwizard.features.auth.dto.RegisterRequest;
import com.tuan.debtwizard.features.auth.dto.RegisterResponse;
import com.tuan.debtwizard.features.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
/*
    @GetMapping("google-success")
    public ResponseEntity<Login> googleSuccess(@AuthenticationPrincipal OAuth2User principal,
                                           HttpServletRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authService.processGoogleLogin(principal, request);
        return ResponseEntity.ok(Map.of("message", "Login with Google success"));
    }
    */

    @PostMapping("register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = authService.register(registerRequest);
        return new ApiResponse<>("Register successful",registerResponse);
    }

    @PostMapping("login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        LoginResponse response = authService.login(loginRequest, request);
        return new ApiResponse<>("Login successful",response);
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @AuthenticationPrincipal UserDetails userDetails
            ) {
            UserResponse response = authService.getCurrentUser(userDetails);
            return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
}