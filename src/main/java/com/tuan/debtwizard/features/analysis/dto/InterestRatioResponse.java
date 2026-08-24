package com.tuan.debtwizard.features.analysis.dto;

import com.tuan.debtwizard.features.analysis.FinanceHealth;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InterestRatioResponse {
    private BigDecimal monthlyInterest;
    private BigDecimal monthlyIncome;
    private double ratio;
    private FinanceHealth financeHealth;
    private String recommendation;
    public InterestRatioResponse(BigDecimal monthlyInterest, BigDecimal monthlyIncome,
                            double ratio, FinanceHealth financeHealth, String recommendation){
        this.monthlyInterest = monthlyInterest;
        this.monthlyIncome = monthlyIncome;
        this.ratio = ratio;
        this.financeHealth = financeHealth;
        this.recommendation = recommendation;
        
    }
}