package com.tuan.debtwizard.features.planning.mapper;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Component
public class SnapshotMapper {

    public List<DebtSnapshot> toSnapshots(List<Debt> debts) {
        List<DebtSnapshot> snapshots = new ArrayList<>();
        for (Debt debt : debts) {
            snapshots.add(toSnapshot(debt));}
        return snapshots;
    }

    public DebtSnapshot toSnapshot(Debt debt) {
        DebtSnapshot snapshot = new DebtSnapshot();
        snapshot.setDebtId(debt.getId());
        snapshot.setDebtName(debt.getLenderName());
        snapshot.setBalance(debt.getRemainingPrincipal().add(debt.getAccruedInterest()));
        snapshot.setInterestRate(debt.getInterestSettings().getInterestRate());
        snapshot.setMinimumPayment(debt.getExpectedMonthlyPayment());
        snapshot.setCurrentPrincipalPaid(BigDecimal.ZERO);
        snapshot.setCurrentMinimumPaid(BigDecimal.ZERO);
        snapshot.setCurrentExtraPaid(BigDecimal.ZERO);
        snapshot.setCurrentInterestPaid(BigDecimal.ZERO);
        snapshot.setPaidOff(false);
        return snapshot;
    }
}