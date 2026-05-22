package com.tuan.debtwizard.dto;

//Debt-to-Income Ratio
import com.tuan.debtwizard.features.analystics.FinanceHealth;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DtiResponse {
    private double dtiRatio;
    private BigDecimal totalMonthlyDebt; // Tổng nợ tháng đó
    private BigDecimal monthlyIncome;    // Thu nhập của user
    private FinanceHealth healthStatus;
    private String advice;            // Lời khuyên của app cho user

}