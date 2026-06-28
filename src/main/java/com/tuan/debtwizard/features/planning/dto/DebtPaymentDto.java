package com.tuan.debtwizard.features.planning.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DebtPaymentDto {
    private Long debtId;
    private String debtName;

    private BigDecimal minimumPaid;
    private BigDecimal extraPaid;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;

    private BigDecimal remainingBalance;
    private boolean paidOff;
    public BigDecimal getPaymentThisMonth() {
        return minimumPaid.add(extraPaid);
    }
}