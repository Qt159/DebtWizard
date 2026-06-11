package com.tuan.debtwizard.features.analysis.dto;

import com.tuan.debtwizard.features.analysis.FinanceHealth;
import lombok.Getter;
import lombok.Setter;

import java.time.Period;
@Getter
@Setter
public class RepaymentTimeResponse {
    private int totalDebts;
    private Period averageRepaymentTime;
    private FinanceHealth financeHealth;
    private String recommendation;
}