package com.tuan.debtwizard.features.planning.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.planning.dto.CompareRequest;
import com.tuan.debtwizard.features.planning.dto.CompareResponse;
import com.tuan.debtwizard.features.planning.service.PlanningService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planning")
public class PlanningController {

    private final PlanningService planningService;
    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    @PostMapping("/compare")
    public ApiResponse<CompareResponse> compare(@Valid @RequestBody CompareRequest request) {
        return ApiResponse.success(planningService.comparePlans(request));
    }
}