package com.tuan.debtwizard.features.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class DashboardResponse {
    private BigDecimal totalDebt;
    private BigDecimal totalPaid;
    private BigDecimal remainingDebt;

    private int activeDebtCount;
    private int paidOffDebtCount;
    private int overdueDebtCount;

    private BigDecimal overdueAmount;
    private BigDecimal accruedInterest;    // Lãi phát sinh

    private NextDueDebtInfo nextDueDebt;
    private List<NextDueDebtInfo> upcomingDebts;

    public DashboardResponse(BigDecimal totalDebt, BigDecimal totalPaid,
                           BigDecimal remainingDebt, int activeDebtCount,int paidOffDebtCount ,
                           int overdueDebtCount,BigDecimal overdueAmount, BigDecimal accruedInterest,
                           NextDueDebtInfo nextDueDebt, List<NextDueDebtInfo> upcomingDebts)
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
        this.upcomingDebts = upcomingDebts;
    }
    public DashboardResponse(){}
}
