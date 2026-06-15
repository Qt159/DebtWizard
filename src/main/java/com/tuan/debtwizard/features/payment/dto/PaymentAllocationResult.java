package com.tuan.debtwizard.features.payment.dto;


import lombok.Getter;

import java.math.BigDecimal;
@Getter
public class PaymentAllocationResult {
    private BigDecimal principalPaid;
    private BigDecimal interestPaid;
    private BigDecimal remainingPrincipal;
    private BigDecimal remainingInterest;

    private BigDecimal remainingPayment;
    public PaymentAllocationResult() {}
    public PaymentAllocationResult(BigDecimal principalPaid, BigDecimal interestPaid,
                                   BigDecimal remainingPrincipal, BigDecimal remainingInterest,
                                   BigDecimal remainingPayment) {
        this.principalPaid = principalPaid;
        this.interestPaid = interestPaid;
        this.remainingPrincipal = remainingPrincipal;
        this.remainingInterest = remainingInterest;
        this.remainingPayment = remainingPayment;

    }


}
