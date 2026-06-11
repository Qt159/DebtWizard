package com.tuan.debtwizard.features.recommendation.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
public class ApplyAvalanche implements RepaymentSortingStrategy {
    @Override
    public List<Debt> sort(List<Debt> debts) {
        debts.sort((debt1, debt2) -> {
            BigDecimal debt1InterestRate = BigDecimal.ZERO;
            if (debt1.getInterestConfig() != null) {
                debt1InterestRate = debt1.getInterestConfig().getInterestRate();
            }
            BigDecimal debt2InterestRate = BigDecimal.ZERO;
            if (debt2.getInterestConfig() != null) {
                debt2InterestRate = debt2.getInterestConfig().getInterestRate();
            }
            // Lãi cao xếp trước
            return debt2InterestRate.compareTo(debt1InterestRate);
        });
        return debts;
    }
}
