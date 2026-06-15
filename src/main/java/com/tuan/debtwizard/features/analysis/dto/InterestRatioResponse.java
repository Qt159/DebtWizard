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
    private BigDecimal income;
    private double ratio;
    private FinanceHealth financeHealth;
    private String recommendation;
    public InterestRatioResponse(BigDecimal totalPrincipal, BigDecimal totalInterest,
                                 double ratio, FinanceHealth financeHealth, String recommendation){
        this.totalPrincipal = totalPrincipal;
        this.totalInterest = totalInterest;
        this.ratio = ratio;
        this.financeHealth = financeHealth;
        this.recommendation = recommendation;
    }
}