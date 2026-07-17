package com.tuan.debtwizard.features.analysis.dto;

import com.tuan.debtwizard.features.analysis.FinanceHealth;
import lombok.Getter;
import lombok.Setter;

import java.time.Period;
@Getter
@Setter
public class RepaymentTimeResponse {
    private int totalActiveDebts;
    private Integer estimatedMonths;       // số tháng cần để trả hết
    public RepaymentTimeResponse(int totalActiveDebts, Integer estimatedMonths) {
        this.totalActiveDebts = totalActiveDebts;
        this.estimatedMonths = estimatedMonths;
    }
}