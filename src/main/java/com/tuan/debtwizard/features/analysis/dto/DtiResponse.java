package com.tuan.debtwizard.features.analysis.dto;

import com.tuan.debtwizard.features.analysis.FinanceHealth;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DtiResponse {
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyDebtPayment;
    private double dtiRatio;
    private FinanceHealth financeHealth;
    private String recommendation;
    public DtiResponse() {}
    public DtiResponse(BigDecimal monthlyIncome, BigDecimal monthlyDebtPayment,
                       double dtiRatio, FinanceHealth financeHealth, String recommendation) {
        this.monthlyIncome = monthlyIncome;
        this.monthlyDebtPayment = monthlyDebtPayment;
        this.dtiRatio = dtiRatio;
        this.financeHealth = financeHealth;
        this.recommendation = recommendation;
    }
}
