package com.tuan.debtwizard.features.analysis.dto;

import com.tuan.debtwizard.features.analysis.FinanceHealth;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InterestRatioResponse {
    private BigDecimal totalPrincipal;
    private BigDecimal totalInterest;
    private double ratio;
    private FinanceHealth financeHealth;
    private String recommendation;
}