package com.tuan.debtwizard.features.recommendation.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import java.util.List;

public interface RepaymentSortingStrategy {
    List<Debt> sort(List<Debt> debts);
}