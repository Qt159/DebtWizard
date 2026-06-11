package com.tuan.debtwizard.features.recommendation.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.auth.model.User;
import com.tuan.debtwizard.features.auth.repository.UserRepository;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.recommendation.model.RepaymentStrategy;
import com.tuan.debtwizard.features.recommendation.dto.DebtRecommendationItem;
import com.tuan.debtwizard.features.recommendation.dto.DebtRecommendationResponse;
import com.tuan.debtwizard.features.recommendation.mapper.DebtRecommendationMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DebtRecommendationService {
    private final UserRepository userRepository;
    private final DebtRepository debtRepository;
    private final DebtRecommendationMapper debtRecommendationMapper;
    private final RepaymentSortingStrategyFactory sortingStrategyFactory;

    public DebtRecommendationService(
            UserRepository userRepository,
            DebtRepository debtRepository,
            DebtRecommendationMapper debtRecommendationMapper,
            RepaymentSortingStrategyFactory sortingStrategyFactory) {
        this.userRepository = userRepository;
        this.debtRepository = debtRepository;
        this.debtRecommendationMapper = debtRecommendationMapper;
        this.sortingStrategyFactory = sortingStrategyFactory;
    }

    public DebtRecommendationResponse recommendDebts(
            UserDetails userDetails,
            RepaymentStrategy repaymentStrategy) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Debt> debts = debtRepository
                .findByUserIdAndStatusAndDeletedFalse(user.getId(), DebtStatus.ACTIVE);
        if (repaymentStrategy == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STRATEGY);}

        RepaymentSortingStrategy sortingStrategy =
                sortingStrategyFactory.get(repaymentStrategy);
        List<Debt> sortedDebts = sortingStrategy.sort(debts);

        List<DebtRecommendationItem> items = new ArrayList<>();
        int priority = 1;
        for (Debt debt : sortedDebts) {
            items.add(debtRecommendationMapper.toItem(debt, priority, repaymentStrategy));
            priority++;
        }
        return new DebtRecommendationResponse(repaymentStrategy, items);
    }
}