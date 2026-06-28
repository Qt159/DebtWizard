package com.tuan.debtwizard.features.planning.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompareResponse {
    private PlanComparisonDto firstPlan;
    private PlanComparisonDto secondPlan;
}