package com.tuan.debtwizard.features.planning.service.strategy;

import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import java.util.List;

public interface DebtSelectionStrategy {
    DebtSnapshot selectTargetDebt(List<DebtSnapshot> activeDebts);
}