package com.tuan.debtwizard.features.payment.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.auth.model.User;
import com.tuan.debtwizard.features.auth.repository.UserRepository;
import com.tuan.debtwizard.features.debt.dto.DebtRecommendationItem;
import com.tuan.debtwizard.features.debt.dto.DebtRecommendationResponse;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.payment.mapper.DebtRecommendationMapper;
import com.tuan.debtwizard.features.payment.model.RepaymentStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DebtRecommendationService {
    private final UserRepository userRepository;
    private final DebtRepository debtRepository;
    private final DebtRecommendationMapper debtRecommendationMapper;

    public DebtRecommendationService(UserRepository userRepository, DebtRepository debtRepository, DebtRecommendationMapper debtRecommendationMapper) {
        this.userRepository = userRepository;
        this.debtRepository = debtRepository;
        this.debtRecommendationMapper = debtRecommendationMapper;
    }

    public DebtRecommendationResponse recommendDebts(
            UserDetails userDetails, RepaymentStrategy repaymentStrategy) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Debt> debts = debtRepository.findByUserIdAndStatusAndDeletedFalse(user.getId(), DebtStatus.ACTIVE);

        if (repaymentStrategy == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STRATEGY);
        }
        RepaymentSortingStrategy sortingStrategy;
        if (repaymentStrategy == RepaymentStrategy.SNOWBALL) {
            sortingStrategy = new ApplySnowball();
        } else if (repaymentStrategy == RepaymentStrategy.AVALANCHE) {
            sortingStrategy = new ApplyAvalanche();
        }
        else {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STRATEGY);
        }

        List<Debt> sortedDebts = sortingStrategy.sort(debts);
        List<DebtRecommendationItem> items = new ArrayList<>();
        int priority = 1;
        for (Debt debt : sortedDebts) {
            items.add(debtRecommendationMapper.toItem(debt, priority));
            priority++;
        }
        return new DebtRecommendationResponse(repaymentStrategy, items);
    }
}