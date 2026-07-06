package com.tuan.debtwizard.features.planning.helper;

import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;


@Component
public class PaymentHelper {
    public void resetMonthlyTracking(List<DebtSnapshot> debts) {
        for (DebtSnapshot debt : debts) {
            debt.resetMonthlyTracking();
        }
    }

    public void applyMinimumPayments(List<DebtSnapshot> debts) {
        for (DebtSnapshot debt : debts) {
            if (!debt.hasBalance()) {
                continue;}
            BigDecimal payment = debt.getMinimumPayment().min(debt.getBalance());
            debt.setBalance(debt.getBalance().subtract(payment));
            debt.setCurrentMinimumPaid(payment);
            debt.setCurrentPrincipalPaid(debt.getCurrentPrincipalPaid().add(payment));
        }
    }
    public BigDecimal applyExtraPayment(BigDecimal monthlyExtraBudget, DebtSnapshot targetDebt) {
        if (targetDebt == null || !targetDebt.hasBalance()) {
            return BigDecimal.ZERO;
        }
        BigDecimal extra = monthlyExtraBudget.min(targetDebt.getBalance());
        targetDebt.setBalance(targetDebt.getBalance().subtract(extra));
        targetDebt.setCurrentExtraPaid(extra);
        targetDebt.setCurrentPrincipalPaid(targetDebt.getCurrentPrincipalPaid().add(extra));
        return extra;
    }


    public BigDecimal applyMonthlyInterest(List<DebtSnapshot> debts) {
        BigDecimal totalInterest = BigDecimal.ZERO;
        for (DebtSnapshot debt : debts) {
            if (!debt.hasBalance()) {
                continue;}
            BigDecimal interest = debt.getBalance()
                    .multiply(debt.getInterestRate()).divide(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

            debt.setBalance(debt.getBalance().add(interest));
            debt.setCurrentInterestPaid(interest);
            totalInterest = totalInterest.add(interest);
        }
        return totalInterest;
    }

    public BigDecimal releaseCashflow(List<DebtSnapshot> debts) {
        BigDecimal released = BigDecimal.ZERO;
        for (DebtSnapshot debt : debts) {
            if (!debt.isPaidOff() && !debt.hasBalance()) {
                debt.setPaidOff(true);
                released = released.add(debt.getMinimumPayment());
            }
        }
        return released;
    }
}