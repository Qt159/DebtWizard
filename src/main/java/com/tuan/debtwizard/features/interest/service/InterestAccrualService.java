package com.tuan.debtwizard.features.interest.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class InterestAccrualService {
    private final InterestCalculationService interestCalculationService;
    public InterestAccrualService(
            InterestCalculationService interestCalculationService) {
        this.interestCalculationService = interestCalculationService;}

    public void accrueInterest(Debt debt, LocalDate toDate) {

        if (debt == null || toDate == null) {return;}

        LocalDate fromDate = debt.getLastInterestAccruedDate();

        if (fromDate == null) {fromDate = debt.getStartDate();}
        if (!toDate.isAfter(fromDate)) {return;}
        BigDecimal interest =
                interestCalculationService.calculateInterest(debt, fromDate, toDate);
        BigDecimal currentInterest =
                debt.getAccruedInterest() == null
                        ? BigDecimal.ZERO
                        : debt.getAccruedInterest();
        debt.setAccruedInterest(currentInterest.add(interest));
        debt.setLastInterestAccruedDate(toDate);
    }
}