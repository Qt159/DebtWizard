package com.tuan.debtwizard.features.planning.dto;

import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PlanComparisonDto {
    private RepaymentStrategy strategy;
    private String planName;
    private BigDecimal totalInterestPaid;
    private int payoffDurationMonths;
}