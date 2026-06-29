package com.tuan.debtwizard.features.planning.service.strategy;

import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ImproveCashflowStrategy implements DebtSelectionStrategy {

    @Override
    public DebtSnapshot selectTargetDebt(List<DebtSnapshot> activeDebts, BigDecimal extraPaymentAllocation) {
        DebtSnapshot target = null;
        BigDecimal bestScore = BigDecimal.valueOf(-1);

        for (DebtSnapshot debt : activeDebts) {

            BigDecimal totalMonthlyPayment = debt.getMinimumPayment().add(extraPaymentAllocation);
            if (totalMonthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal payoffMonths = debt.getBalance()
                    .divide(totalMonthlyPayment, 2, RoundingMode.HALF_UP);

            if (payoffMonths.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // PriorityScore = MonthlyPayment / EstimatedPayoffMonths
            BigDecimal score = totalMonthlyPayment
                    .divide(payoffMonths, 2, RoundingMode.HALF_UP);

            if (target == null || score.compareTo(bestScore) > 0) {
                target = debt;
                bestScore = score;
            }
        }
        return target;
    }
}
