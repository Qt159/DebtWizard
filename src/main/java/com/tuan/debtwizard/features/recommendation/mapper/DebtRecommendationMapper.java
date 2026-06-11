package com.tuan.debtwizard.features.recommendation.mapper;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.recommendation.model.RepaymentStrategy;
import com.tuan.debtwizard.features.recommendation.dto.DebtRecommendationItem;
import com.tuan.debtwizard.features.recommendation.dto.DebtRecommendationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.tuan.debtwizard.features.recommendation.model.RepaymentStrategy.AVALANCHE;
import static com.tuan.debtwizard.features.recommendation.model.RepaymentStrategy.SNOWBALL;

@Component
public class DebtRecommendationMapper {
    public DebtRecommendationItem toItem(Debt debt, Integer priority, RepaymentStrategy strategy) {
        DebtRecommendationItem debtRecommendationItem = new DebtRecommendationItem();
        debtRecommendationItem.setPriority(priority);
        debtRecommendationItem.setDebtId(debt.getId());
        debtRecommendationItem.setLenderName(debt.getLenderName());
        debtRecommendationItem.setRemainingPrincipal(debt.getRemainingPrincipal());
        debtRecommendationItem.setInterestRate(debt.getInterestConfig().getInterestRate());
        switch (strategy) {
            case SNOWBALL:
                debtRecommendationItem.setRecommendationReason("Khoản nợ nhỏ nhất theo Snowball");
                break;
            case AVALANCHE:
                debtRecommendationItem.setRecommendationReason("Khoản nợ lãi suất cao nhất theo Avalanche");
                break;
            default:
                debtRecommendationItem.setRecommendationReason("Theo chiến lược " + strategy);
        }

        return debtRecommendationItem;
    }
    public DebtRecommendationResponse toResponse(RepaymentStrategy repaymentStrategy,
                                                 List<DebtRecommendationItem> debtRecommendationItems) {
        return new DebtRecommendationResponse(repaymentStrategy, debtRecommendationItems);
    }
}
