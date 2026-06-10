package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.payment.model.RepaymentStrategy;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class DebtRecommendationResponse {
    private RepaymentStrategy repaymentStrategy;
    private List<DebtRecommendationItem> debts;

    public DebtRecommendationResponse() {}

    public DebtRecommendationResponse(RepaymentStrategy repaymentStrategy, List<DebtRecommendationItem> items) {
        this.repaymentStrategy = repaymentStrategy;
        this.debts = items;
    }
}