package com.tuan.debtwizard.features.debt.service.interest;

import com.tuan.debtwizard.features.debt.model.Debt;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InterestCalculationStrategy {
    BigDecimal calculateInterest(Debt debt, LocalDate fromDate, LocalDate toDate);
}