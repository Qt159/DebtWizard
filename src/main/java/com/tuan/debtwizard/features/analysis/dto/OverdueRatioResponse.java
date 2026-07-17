package com.tuan.debtwizard.features.analysis.dto;

import com.tuan.debtwizard.features.analysis.FinanceHealth;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OverdueRatioResponse {
    private int totalActiveDebts;
    private int overdueDebts;
    private double overdueRatio;
    private FinanceHealth financeHealth;
    private String recommendation;

    public OverdueRatioResponse(int totalActiveDebts, int overdueDebts, double ratio,
                                FinanceHealth health, String defaultAdvice) {
        this.totalActiveDebts = totalActiveDebts;
        this.overdueDebts = overdueDebts;
        this.overdueRatio = ratio;
        this.financeHealth = health;
        this.recommendation = defaultAdvice;
    }
}
