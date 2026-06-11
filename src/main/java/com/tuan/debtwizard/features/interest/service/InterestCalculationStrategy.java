package com.tuan.debtwizard.features.interest.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.interest.model.InterestConfig;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InterestCalculationStrategy {
    BigDecimal calculateInterest(Debt debt, InterestConfig config, LocalDate fromDate, LocalDate toDate);
}