package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.payment.model.PaymentAllocationStrategy;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DebtRecommendationResponse {
    private PaymentAllocationStrategy paymentAllocationStrategy;
    private List<DebtRecommendationItem> debts;

    public DebtRecommendationResponse() {
    }

    public DebtRecommendationResponse(PaymentAllocationStrategy paymentAllocationStrategy, List<DebtRecommendationItem> items) {
        this.paymentAllocationStrategy = paymentAllocationStrategy;
        this.debts = items;
    }

}
