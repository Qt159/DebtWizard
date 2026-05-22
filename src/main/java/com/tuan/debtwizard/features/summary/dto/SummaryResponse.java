package com.tuan.debtwizard.features.summary.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SummaryResponse {
    private BigDecimal totalDebt;
    private BigDecimal totalPaid;
    private BigDecimal remainingDebt;
    private int debtCount;
    private int overdueCount;
    private int paidOffDebtCount;
    private BigDecimal overdueAmount;

    public SummaryResponse(BigDecimal totalDebt, BigDecimal totalPaid,
                           BigDecimal remainingDebt, int debtCount,
                           int overdueCount,BigDecimal overdueAmount, int paidOffDebtCount )
    {
        this.totalDebt = totalDebt;
        this.totalPaid = totalPaid;
        this.remainingDebt = remainingDebt;
        this.debtCount = debtCount;
        this.overdueCount = overdueCount;
        this.paidOffDebtCount = paidOffDebtCount;
        this.overdueAmount = overdueAmount;
    }
    public SummaryResponse() {}
}
