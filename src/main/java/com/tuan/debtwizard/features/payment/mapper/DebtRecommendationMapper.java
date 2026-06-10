package com.tuan.debtwizard.features.payment.mapper;

import com.tuan.debtwizard.features.debt.dto.DebtRecommendationItem;
import com.tuan.debtwizard.features.debt.dto.DebtRecommendationResponse;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.payment.model.RepaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DebtRecommendationMapper {
    public DebtRecommendationItem toItem(Debt debt, Integer priority) {
        DebtRecommendationItem debtRecommendationItem = new DebtRecommendationItem();
        debtRecommendationItem.setPriority(priority);
        debtRecommendationItem.setDebtId(debt.getId());
        debtRecommendationItem.setLenderName(debt.getLenderName());
        debtRecommendationItem.setRemainingPrincipal(debt.getRemainingPrincipal());
        debtRecommendationItem.setInterestRate(debt.getInterestConfig().getInterestRate());
        return debtRecommendationItem;
    }
    public DebtRecommendationResponse toResponse(RepaymentStrategy repaymentStrategy,
                                                 List<DebtRecommendationItem> debtRecommendationItems) {
        return new DebtRecommendationResponse(repaymentStrategy, debtRecommendationItems);
    }
}
