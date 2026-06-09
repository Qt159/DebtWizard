package com.tuan.debtwizard.features.interest.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.interest.model.InterestCalculationMethod;
import com.tuan.debtwizard.features.interest.model.InterestConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class InterestCalculationService {

    public BigDecimal calculateInterest(
            Debt debt,
            LocalDate fromDate,
            LocalDate toDate) {

        if (debt == null || fromDate == null || toDate == null || !toDate.isAfter(fromDate)) {
            return BigDecimal.ZERO;
        }

        InterestConfig config = debt.getInterestConfig();
        if (config == null) {return BigDecimal.ZERO;}

        InterestCalculationMethod method =
                config.getInterestCalculationMethod();

        return switch (method) {
            case FLAT ->
                    calculateFlatInterest(debt, config, fromDate, toDate);
            case REDUCING_BALANCE ->
                    calculateReducingBalanceInterest(debt, config, fromDate, toDate);
        };
    }

    private BigDecimal calculateFlatInterest(
            Debt debt,
            InterestConfig config,
            LocalDate fromDate,
            LocalDate toDate) {

        BigDecimal principal = debt.getTotalPrincipal();

        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return principal
                .multiply(calculateRateForPeriod(config, fromDate, toDate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    private BigDecimal calculateReducingBalanceInterest(
            Debt debt,
            InterestConfig config,
            LocalDate fromDate,
            LocalDate toDate) {
        BigDecimal principal = debt.getRemainingPrincipal();
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return principal
                .multiply(calculateRateForPeriod(config, fromDate, toDate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRateForPeriod(
            InterestConfig config,
            LocalDate fromDate,
            LocalDate toDate) {
        long days = ChronoUnit.DAYS.between(fromDate, toDate);
        BigDecimal rate = config.getInterestRate();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return switch (config.getInterestRatePeriod()) {
            case DAILY ->
                    rate.multiply(BigDecimal.valueOf(days));
            case MONTHLY ->
                    rate.divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(days));
            case ANNUALLY ->
                    rate.divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(days));
        };
    }
}