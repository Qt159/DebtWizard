package com.tuan.debtwizard.features.analysis.dto;

import com.tuan.debtwizard.features.analysis.FinanceHealth;
import lombok.Getter;
import lombok.Setter;

import java.time.Period;
@Getter
@Setter
public class RepaymentTimeResponse {
    private int totalDebts;
    private int repaymentMonths;       // số tháng cần để trả hết
    private String recommendation;
    public RepaymentTimeResponse(int totalDebts, int repaymentMonths) {
        this.totalDebts = totalDebts;
        this.repaymentMonths = repaymentMonths;
    }
}