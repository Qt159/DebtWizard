package com.tuan.debtwizard.features.planning.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.planning.dto.CompareRequest;
import com.tuan.debtwizard.features.planning.dto.CompareResponse;
import com.tuan.debtwizard.features.planning.service.PlanningService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planning")
@SecurityRequirement(name = "Bearer Authentication")
public class PlanningController {

    private final PlanningService planningService;
    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    @PostMapping("/compare")
    public ApiResponse<CompareResponse> compare(@Valid @RequestBody CompareRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(planningService.comparePlans(request, userDetails));
    }
}