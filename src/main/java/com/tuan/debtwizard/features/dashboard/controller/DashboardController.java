package com.tuan.debtwizard.features.dashboard.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.dashboard.dto.DashboardResponse;
import com.tuan.debtwizard.features.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard(@Valid Authentication authentication) {
        return ApiResponse.success(dashboardService.getDashboard(authentication.getName()));
    }
}
