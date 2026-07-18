package com.tuan.debtwizard.features.planning.service.strategy;

import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class ImproveCashflowStrategy implements DebtSelectionStrategy {


     /*Chọn khoản nợ có thể trả hết nhanh nhất để giải phóng cashflow sớm nhất.
     Ưu tiên debt có balance / minimumPayment thấp nhất (payoff gần nhất)*/
    @Override
    public DebtSnapshot selectTargetDebt(List<DebtSnapshot> activeDebts, BigDecimal extraPaymentAllocation) {
        DebtSnapshot target = null;
        BigDecimal bestScore = BigDecimal.valueOf(-1);

        for (DebtSnapshot debt : activeDebts) {
            if (debt.getMinimumPayment().compareTo(BigDecimal.ZERO) <= 0
                    || debt.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // estimatedPayoffMonths = balance / minimumPayment
            BigDecimal payoffMonths = debt.getBalance()
                    .divide(debt.getMinimumPayment(), 4, RoundingMode.HALF_UP);

            if (payoffMonths.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // score = minimumPayment / estimatedPayoffMonths
            BigDecimal score = debt.getMinimumPayment()
                    .divide(payoffMonths, 4, RoundingMode.HALF_UP);

            if (target == null || score.compareTo(bestScore) > 0) {
                target = debt;
                bestScore = score;
            }
        }
        return target;
    }
}
