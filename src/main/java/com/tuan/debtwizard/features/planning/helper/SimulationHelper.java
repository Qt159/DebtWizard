package com.tuan.debtwizard.features.planning.helper;

import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import com.tuan.debtwizard.features.user.model.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class SimulationHelper {

    public BigDecimal calculateMonthlyExtraBudget(User user, List<DebtSnapshot> debts) {
        BigDecimal income = user.getMonthlyIncome();
        BigDecimal expense = user.getMonthlyExpense();

        BigDecimal total = income.subtract(expense);
        BigDecimal minPayment = calculateTotalMinimumPayment(debts);
        return total.subtract(minPayment).max(BigDecimal.ZERO);
    }

    public BigDecimal calculateTotalMinimumPayment(List<DebtSnapshot> debts) {
        BigDecimal total = BigDecimal.ZERO;
        for (DebtSnapshot debt : debts) {
            total = total.add(debt.getMinimumPayment());}
        return total;
    }

    public List<DebtSnapshot> getActiveDebts(List<DebtSnapshot> debts) {
        List<DebtSnapshot> activeDebts = new ArrayList<>();
        for (DebtSnapshot debt : debts) {
            if (debt.hasBalance()) {
                activeDebts.add(debt);}
        }
        return activeDebts;
    }

    public boolean hasActiveDebt(List<DebtSnapshot> debts) {
        for (DebtSnapshot debt : debts) {
            if (debt.hasBalance()) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal calculateTotalPayment(List<DebtSnapshot> debts) {
        BigDecimal total = BigDecimal.ZERO;
        for (DebtSnapshot debt : debts) {
            total = total.add(debt.getCurrentMinimumPaid()).add(debt.getCurrentExtraPaid());
        }
        return total;
    }
}