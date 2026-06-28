package com.tuan.debtwizard.features.planning.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DebtSnapshot {
    private Long debtId;
    private String debtName;
    private BigDecimal balance;
    private BigDecimal interestRate;
    private BigDecimal minimumPayment;
 //Monthly tracking
    private BigDecimal currentMinimumPaid = BigDecimal.ZERO;
    private BigDecimal currentExtraPaid = BigDecimal.ZERO;
    private BigDecimal currentPrincipalPaid = BigDecimal.ZERO;
    private BigDecimal currentInterestPaid = BigDecimal.ZERO;
    private boolean paidOff = false;
    public boolean hasBalance() {
        return balance.compareTo(BigDecimal.ZERO) > 0;}

    public BigDecimal getPaymentThisMonth() {
        return currentMinimumPaid.add(currentExtraPaid);}

    public void resetMonthlyTracking() {
        currentMinimumPaid = BigDecimal.ZERO;
        currentExtraPaid = BigDecimal.ZERO;
        currentPrincipalPaid = BigDecimal.ZERO;
        currentInterestPaid = BigDecimal.ZERO;
    }
}