package com.tuan.debtwizard.features.planning.service.strategy;

import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class MinimizeInterestStrategy implements DebtSelectionStrategy {
    @Override
    public DebtSnapshot selectTargetDebt(List<DebtSnapshot> activeDebts) {
        if (activeDebts.isEmpty()) {
            return null;}
        DebtSnapshot target = null;
        for (DebtSnapshot debt : activeDebts) {
            if (target == null) {
                target = debt;
                continue;}
            if (debt.getInterestRate().compareTo(target.getInterestRate()) > 0) {
                target = debt;
            }
        }
        return target;
    }
}