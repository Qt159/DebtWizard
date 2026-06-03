package com.tuan.debtwizard.features.interest.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.interest.model.InterestConfig;
import com.tuan.debtwizard.features.interest.model.InterestRatePeriod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class InterestCalculationService {
    public BigDecimal calculateInterest(Debt debt) {

        switch (debt.getInterestConfig().getInterestCalculationMethod()) {
            case FLAT:
                return calculateFlatInterest(debt);
            case REDUCING_BALANCE:
                return calculateReducingBalanceInterest(debt);
            default:
                throw new IllegalArgumentException("Unsupported method");
        }
    }
    private BigDecimal calculateFlatInterest(Debt debt) {
        InterestConfig config = debt.getInterestConfig();
        BigDecimal rate = config.getInterestRate();
        if (config.getInterestRatePeriod() == InterestRatePeriod.ANNUALLY){
            rate = rate.divide(BigDecimal.valueOf(12),2, RoundingMode.HALF_UP);
        }
        return debt.getTotalPrincipal()
                .multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateReducingBalanceInterest(Debt debt) {
        InterestConfig config = debt.getInterestConfig();
        BigDecimal rate = config.getInterestRate();
        if (config.getInterestRatePeriod() == InterestRatePeriod.ANNUALLY){
            rate = rate.divide(BigDecimal.valueOf(12),2, RoundingMode.HALF_UP);
        }
        return debt.getRemainingPrincipal().multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}