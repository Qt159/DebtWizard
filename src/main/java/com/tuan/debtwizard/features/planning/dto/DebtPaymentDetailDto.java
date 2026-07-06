package com.tuan.debtwizard.features.planning.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DebtPaymentDetailDto {
    private Long debtId;
    private String debtName;
    private BigDecimal minimumPaid;
    private BigDecimal extraPaid;
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal totalPaid;
    private BigDecimal remainingBalance;
    private boolean paidOff;
}
