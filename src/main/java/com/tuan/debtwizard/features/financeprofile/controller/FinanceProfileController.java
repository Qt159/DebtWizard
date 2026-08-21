package com.tuan.debtwizard.features.financeprofile.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.financeprofile.dto.FinanceProfileResponse;
import com.tuan.debtwizard.features.financeprofile.dto.UpdateFinanceProfileRequest;
import com.tuan.debtwizard.features.financeprofile.service.FinanceProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance-profile")
@SecurityRequirement(name = "Bearer Authentication")
public class FinanceProfileController {

    private final FinanceProfileService financeProfileService;

    public FinanceProfileController(FinanceProfileService financeProfileService) {
        this.financeProfileService = financeProfileService;
    }

    @GetMapping
    public ApiResponse<FinanceProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(financeProfileService.getProfile(userDetails));
    }

    @PutMapping
    public ApiResponse<FinanceProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateFinanceProfileRequest request) {
        return ApiResponse.success(financeProfileService.updateProfile(userDetails, request));
    }
}
