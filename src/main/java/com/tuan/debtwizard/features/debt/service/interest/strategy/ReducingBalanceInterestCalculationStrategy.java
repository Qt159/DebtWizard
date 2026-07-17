package com.tuan.debtwizard.features.debt.service.interest.strategy;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.InterestFrequency;
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
        BigDecimal interestRate = settings.getInterestRate();

        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return switch (settings.getInterestFrequency()) {
            case DAILY -> interestRate.divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
            case MONTHLY -> interestRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
            case ANNUALLY -> interestRate;
        };
    }

    private BigDecimal getTimeMultiplier(InterestFrequency frequency, long days) {
        return switch (frequency) {
            case DAILY -> BigDecimal.valueOf(days);
            case MONTHLY -> BigDecimal.valueOf(days).divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP);
            case ANNUALLY -> BigDecimal.valueOf(days).divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        };
    }

    @Override
    public BigDecimal calculateInterest(Debt debt, LocalDate fromDate, LocalDate toDate) {
        InterestSettings settings = debt.getInterestSettings();

        if (settings == null || settings.getInterestFrequency() == null) {
            return BigDecimal.ZERO;
        }

        long days = ChronoUnit.DAYS.between(fromDate, toDate);

        if (days <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal remainingPrincipal = debt.getRemainingPrincipal();

        if (remainingPrincipal == null || remainingPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal periodRate = getPeriodRate(settings);
        BigDecimal timeMultiplier = getTimeMultiplier(settings.getInterestFrequency(), days);

        return remainingPrincipal.multiply(periodRate)
                .multiply(timeMultiplier)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /*
    M = P × [r(1+r)^n] / [(1+r)^n - 1]

    P = principal
    r = annualRate / 100 / 12
    n = termMonths
    */
    @Override
    public BigDecimal calculateMonthlyPayment(BigDecimal principal, int termMonths, BigDecimal annualRate) {

        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (termMonths <= 0) {
            return BigDecimal.ZERO;
        }

        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal power = BigDecimal.ONE.add(monthlyRate).pow(termMonths);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);

        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}