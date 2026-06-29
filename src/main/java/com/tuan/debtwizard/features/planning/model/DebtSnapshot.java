package com.tuan.debtwizard.features.planning.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
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

    public DebtSnapshot(DebtSnapshot other) {
        this.debtId = other.debtId;
        this.debtName = other.debtName;
        this.balance = other.balance;
        this.interestRate = other.interestRate;
        this.minimumPayment = other.minimumPayment;
        this.currentMinimumPaid = other.currentMinimumPaid;
        this.currentExtraPaid = other.currentExtraPaid;
        this.currentPrincipalPaid = other.currentPrincipalPaid;
        this.currentInterestPaid = other.currentInterestPaid;
        this.paidOff = other.paidOff;
    }
}