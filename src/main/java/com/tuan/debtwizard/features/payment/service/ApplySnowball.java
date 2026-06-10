package com.tuan.debtwizard.features.payment.service;

import com.tuan.debtwizard.features.debt.model.Debt;

import java.math.BigDecimal;
import java.util.List;

public class ApplySnowball implements RepaymentSortingStrategy {
    @Override
    public List<Debt> sort(List<Debt> debts) {
        debts.sort((debt1, debt2) -> {
            BigDecimal debt1Principal = BigDecimal.ZERO;
            if (debt1.getRemainingPrincipal() != null) {
                debt1Principal = debt1.getRemainingPrincipal();}
            BigDecimal debt2Principal = BigDecimal.ZERO;
            if (debt2.getRemainingPrincipal() != null) {
                debt2Principal = debt2.getRemainingPrincipal();}
            // nợ nhỏ xếp trước
            return debt1Principal.compareTo(debt2Principal);
        });
        return debts;
        }
}
