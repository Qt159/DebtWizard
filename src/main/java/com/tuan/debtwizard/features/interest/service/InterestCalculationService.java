package com.tuan.debtwizard.features.interest.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.interest.model.InterestCalculationMethod;
import com.tuan.debtwizard.features.interest.model.InterestConfig;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;


@Service
public class InterestCalculationService {
    private final InterestCalculationStrategyFactory factory;

    public InterestCalculationService(InterestCalculationStrategyFactory factory) {
        this.factory = factory;
    }

    public BigDecimal calculateInterest(
            Debt debt,
            LocalDate fromDate,
            LocalDate toDate) {

        if (debt == null || fromDate == null || toDate == null || !toDate.isAfter(fromDate)) {
            return BigDecimal.ZERO;
        }
        InterestConfig config = debt.getInterestConfig();

        if (config == null) {
            return BigDecimal.ZERO;
        }
        return factory.get(config.getInterestCalculationMethod())
                .calculateInterest(debt, config, fromDate, toDate);
    }
}