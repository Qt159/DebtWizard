package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class DebtStateService {
    public void refreshDebtStatus(Debt debt){
        if(debt.getRemainingPrincipal().compareTo(BigDecimal.ZERO) <= 0){
            debt.setStatus(DebtStatus.PAID_OFF);
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate firstDueDate = calculateFirstDueDate(debt);
        if (today.isAfter(firstDueDate)) {
            debt.setStatus(DebtStatus.OVERDUE);
            return;
        }
        debt.setStatus(DebtStatus.ACTIVE);
    }

    private LocalDate calculateFirstDueDate(Debt debt) {
        LocalDate start = debt.getStartDate();
        if (start.getDayOfMonth() <= debt.getDueDay()) {
            return start.withDayOfMonth(debt.getDueDay());
        }
        return start.plusMonths(1)
                .withDayOfMonth(debt.getDueDay());
    }

}