package com.tuan.debtwizard.features.analysis.dto;

import com.tuan.debtwizard.features.analysis.FinanceHealth;
import lombok.Getter;
import lombok.Setter;

import java.time.Period;
@Getter
@Setter
public class RepaymentTimeResponse {
    private int totalActiveDebts;
    private int repaymentMonths;       // số tháng cần để trả hết
    private String recommendation;
    public RepaymentTimeResponse(int totalActiveDebts, int repaymentMonths) {
        this.totalActiveDebts = totalActiveDebts;
        this.repaymentMonths = repaymentMonths;
    }
}