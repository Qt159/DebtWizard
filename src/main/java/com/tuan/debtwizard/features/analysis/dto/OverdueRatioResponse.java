package com.tuan.debtwizard.features.analysis.dto;

import com.tuan.debtwizard.features.analysis.FinanceHealth;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OverdueRatioResponse {
    private int totalActiveDebts;
    private int overdueDebts;
    private double overdueRatio;
    private FinanceHealth financeHealth;
    private String recommendation;
}
