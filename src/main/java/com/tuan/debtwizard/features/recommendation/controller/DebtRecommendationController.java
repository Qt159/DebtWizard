package com.tuan.debtwizard.features.recommendation.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.recommendation.dto.DebtRecommendationResponse;
import com.tuan.debtwizard.features.recommendation.model.RepaymentStrategy;
import com.tuan.debtwizard.features.recommendation.service.DebtRecommendationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class DebtRecommendationController {
    private final DebtRecommendationService recommendationService;

    public DebtRecommendationController(DebtRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    @GetMapping
    public ApiResponse<DebtRecommendationResponse> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "SNOWBALL") RepaymentStrategy strategy) {
        return ApiResponse.success(recommendationService.recommendDebts(userDetails, strategy));
    }

}
