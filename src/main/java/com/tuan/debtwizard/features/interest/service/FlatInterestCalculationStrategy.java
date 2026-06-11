package com.tuan.debtwizard.features.interest.strategy;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.interest.model.InterestConfig;
import com.tuan.debtwizard.features.interest.service.InterestCalculationStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class FlatInterestCalculationStrategy implements InterestCalculationStrategy {

    private BigDecimal calculateRateForPeriod(InterestConfig config, LocalDate fromDate, LocalDate toDate) {
        long days = ChronoUnit.DAYS.between(fromDate, toDate);
        BigDecimal rate = config.getInterestRate();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        return switch (config.getInterestRatePeriod()) {
            case DAILY -> rate.multiply(BigDecimal.valueOf(days));
            case MONTHLY -> rate.divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(days));
            case ANNUALLY -> rate.divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(days));
        };
    }

    @Override
    public BigDecimal calculateInterest(Debt debt, InterestConfig config, LocalDate fromDate, LocalDate toDate) {
        BigDecimal principal = debt.getTotalPrincipal();
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        return principal.multiply(calculateRateForPeriod(config, fromDate, toDate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}