package com.tuan.debtwizard.features.planning.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SimulationPayment {

    private Long debtId;
    private String debtName;

    private BigDecimal minimumPaid = BigDecimal.ZERO;
    private BigDecimal extraPaid = BigDecimal.ZERO;
    private BigDecimal principalPaid = BigDecimal.ZERO;
    private BigDecimal interestPaid = BigDecimal.ZERO;

    private BigDecimal remainingBalance = BigDecimal.ZERO;
    private boolean paidOff;

    public BigDecimal getPaymentThisMonth() {
        return minimumPaid.add(extraPaid);
    }
}