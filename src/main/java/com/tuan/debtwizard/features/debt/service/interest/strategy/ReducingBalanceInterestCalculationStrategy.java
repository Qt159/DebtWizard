package com.tuan.debtwizard.features.debt.service.interest.strategy;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.InterestSettings;
import com.tuan.debtwizard.features.debt.service.interest.InterestCalculationStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class ReducingBalanceInterestCalculationStrategy implements InterestCalculationStrategy {

    private BigDecimal getPeriodRate(InterestSettings settings) {
        BigDecimal baseRate = settings.getInterestRate();
        if (baseRate == null || baseRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return switch (settings.getInterestFrequency()) {
            case DAILY -> baseRate.divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
            case MONTHLY -> baseRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
            case ANNUALLY -> baseRate;
        };
    }

    @Override
    public BigDecimal calculateInterest(Debt debt, LocalDate fromDate, LocalDate toDate) {
        long days = ChronoUnit.DAYS.between(fromDate, toDate);
        BigDecimal periodRate = getPeriodRate(debt.getInterestSettings());
        BigDecimal principal = debt.getRemainingPrincipal();
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        // DAILY → nhân số ngày
        // MONTHLY → convert days thành months
        // ANNUALLY → convert days thành years
        BigDecimal multiplier = switch (debt.getInterestSettings().getInterestFrequency()) {
            case DAILY -> BigDecimal.valueOf(days);
            case MONTHLY -> BigDecimal.valueOf(ChronoUnit.MONTHS.between(fromDate, toDate));
            case ANNUALLY -> BigDecimal.valueOf(ChronoUnit.YEARS.between(fromDate, toDate));
        };
        return principal.multiply(periodRate).multiply(multiplier)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}