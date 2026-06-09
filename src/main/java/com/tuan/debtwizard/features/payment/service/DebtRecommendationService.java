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
import com.tuan.debtwizard.features.payment.model.PaymentAllocationStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DebtRecommendationService {
    private final UserRepository userRepository;
    private final DebtRepository debtRepository;
    private final DebtRecommendationMapper debtRecommendationMapper;
    public DebtRecommendationService(UserRepository userRepository, DebtRepository debtRepository, DebtRecommendationMapper debtRecommendationMapper){
        this.userRepository = userRepository;
        this.debtRepository = debtRepository;
        this.debtRecommendationMapper = debtRecommendationMapper;
    }
    public DebtRecommendationResponse recommendDebts(
            UserDetails userDetails,PaymentAllocationStrategy paymentAllocationStrategy) {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Debt> debts = debtRepository.findByUserIdAndStatusAndDeletedFalse(user.getId(), DebtStatus.ACTIVE);
        if(paymentAllocationStrategy.equals(PaymentAllocationStrategy.SNOWBALL)){
            applySnowball(debts);}
        else if(paymentAllocationStrategy.equals(PaymentAllocationStrategy.AVALANCHE)) {
            applyAvalanche(debts);}
        else{
            throw new AppException(ErrorCode.INVALID_PAYMENT_STRATEGY);
        }
        List<DebtRecommendationItem> items = new ArrayList<>();
        int priority = 1;
        for (Debt debt : debts) {
            items.add(debtRecommendationMapper.toItem(debt, priority));
            priority++;
        }
            return new DebtRecommendationResponse(paymentAllocationStrategy, items);

    }

    private List<Debt> applySnowball(List<Debt> debts) {
        debts.sort((debt1, debt2) -> {
            BigDecimal debt1Principal = BigDecimal.ZERO;
            if (debt1.getRemainingPrincipal() != null) {
                debt1Principal = debt1.getRemainingPrincipal();}
            BigDecimal debt2Principal = BigDecimal.ZERO;
            if (debt2.getRemainingPrincipal() != null) {
                debt2Principal = debt2.getRemainingPrincipal();}
            return debt1Principal.compareTo(debt2Principal);
        });
        return debts;}

    private List<Debt> applyAvalanche(List<Debt> debts) {
        debts.sort((debt1, debt2) -> {
            BigDecimal debt1InterestRate = BigDecimal.ZERO;
            if (debt1.getInterestConfig() != null) {
                debt1InterestRate = debt1.getInterestConfig().getInterestRate();}
            BigDecimal debt2InterestRate = BigDecimal.ZERO;
            if (debt2.getInterestConfig() != null) {
                debt2InterestRate = debt2.getInterestConfig().getInterestRate();}
            return debt2InterestRate.compareTo(debt1InterestRate);
        });
        return debts;}
    }

