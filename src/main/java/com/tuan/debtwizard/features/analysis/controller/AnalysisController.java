package com.tuan.debtwizard.features.analysis.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.analysis.dto.AnalysisResponse;
import com.tuan.debtwizard.features.analysis.service.AnalysisService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalysisController {
    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {

        this.analysisService = analysisService;
    }
    @GetMapping("/all")
    public ApiResponse<AnalysisResponse> calculateAllAnalysis(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(analysisService.calculateAllAnalysis(userDetails.getUsername()));
    }
}
