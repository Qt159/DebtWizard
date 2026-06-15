package com.tuan.debtwizard.features.summary.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.summary.dto.SummaryResponse;
import com.tuan.debtwizard.features.summary.service.SummaryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {
    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }
    @GetMapping("/overview")
    public ApiResponse<SummaryResponse> getOverviewSummary(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(summaryService.getSummary(userDetails));
    }
}
