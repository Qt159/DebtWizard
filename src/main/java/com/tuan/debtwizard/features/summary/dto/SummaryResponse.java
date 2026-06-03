package com.tuan.debtwizard.features.summary.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SummaryResponse {
    private BigDecimal totalDebt;
    private BigDecimal totalPaid;
    private BigDecimal remainingDebt;

    private int activeDebtCount;
    private int paidOffDebtCount;
    private int overdueDebtCount;

    private BigDecimal overdueAmount;
    private BigDecimal accruedInterest;    // Lãi phát sinh
    private NextDueDebtInfo nextDueDebt;

    public SummaryResponse(BigDecimal totalDebt, BigDecimal totalPaid,
                           BigDecimal remainingDebt, int activeDebtCount,int paidOffDebtCount ,
                           int overdueDebtCount,BigDecimal overdueAmount, BigDecimal accruedInterest,
                           NextDueDebtInfo nextDueDebt)
    {
        this.totalDebt = totalDebt;
        this.totalPaid = totalPaid;
        this.remainingDebt = remainingDebt;

        this.activeDebtCount = activeDebtCount;
        this.paidOffDebtCount = paidOffDebtCount;
        this.overdueDebtCount = overdueDebtCount;

        this.overdueAmount = overdueAmount;
        this.accruedInterest = accruedInterest;
        this.nextDueDebt = nextDueDebt;
    }
    public SummaryResponse() {}
}
