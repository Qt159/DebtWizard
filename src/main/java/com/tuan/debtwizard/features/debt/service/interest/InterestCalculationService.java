package com.tuan.debtwizard.features.debt.service.interest;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.InterestSettings;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;


@Service
public class InterestCalculationService {
    private final InterestCalculationStrategyFactory factory;

    public InterestCalculationService(InterestCalculationStrategyFactory factory) {
        this.factory = factory;
    }

    public BigDecimal calculateInterest(Debt debt, LocalDate fromDate, LocalDate toDate) {

        if (debt == null || fromDate == null || toDate == null || !toDate.isAfter(fromDate)) {
            return BigDecimal.ZERO;
        }
        InterestSettings settings = debt.getInterestSettings();
        if (settings == null) {
            return BigDecimal.ZERO;
        }
        return factory.get(settings.getInterestCalculationMethod())
                .calculateInterest(debt, fromDate, toDate);
    }

    public BigDecimal calculateMonthlyPayment(Debt debt) {
        InterestSettings settings = debt.getInterestSettings();
        if (settings == null) {
            return debt.getTotalPrincipal()
                    .divide(BigDecimal.valueOf(debt.getTermMonths()),
                            2, java.math.RoundingMode.HALF_UP);
        }
    
        BigDecimal principal =  debt.getTotalPrincipal();
        return factory.get(settings.getInterestCalculationMethod())
                .calculateMonthlyPayment(principal,
                        debt.getTermMonths(), settings.getInterestRate());
    }
}