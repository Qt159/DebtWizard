package com.tuan.debtwizard.features.analysis.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.analysis.FinanceHealth;
import com.tuan.debtwizard.features.analysis.dto.*;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.service.interest.InterestCalculationService;
import com.tuan.debtwizard.features.financeprofile.model.FinanceProfile;
import com.tuan.debtwizard.features.financeprofile.repository.FinanceProfileRepository;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
public class AnalysisService {

    private final UserRepository userRepository;
    private final DebtRepository debtRepository;
    private final FinanceProfileRepository financeProfileRepository;
    private final InterestCalculationService interestCalculationService;

    public AnalysisService(UserRepository userRepository,
                           DebtRepository debtRepository, FinanceProfileRepository financeProfileRepository, InterestCalculationService interestCalculationService) {
        this.userRepository = userRepository;
        this.debtRepository = debtRepository;
        this.financeProfileRepository = financeProfileRepository;
        this.interestCalculationService = interestCalculationService;
    }

    @Transactional(readOnly = true)
    public AnalysisResponse calculateAllAnalysis(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        FinanceProfile financeProfile = financeProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.FINANCE_PROFILE_NOT_FOUND));

        DtiResponse dti = calculateCurrentDti(user, financeProfile);
        InterestRatioResponse interestRatio = calculateInterestRatio(user, financeProfile);
        OverdueRatioResponse overdueRatio = calculateOverdueRatio(user);
        RepaymentTimeResponse repaymentTime = calculateRepaymentTime(user);
        return new AnalysisResponse(dti, interestRatio, overdueRatio, repaymentTime);
    }
    //monthlyPayment / income
    private DtiResponse calculateCurrentDti(User user,FinanceProfile financeProfile) {
        BigDecimal income = financeProfile.getMonthlyIncome();
        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return new DtiResponse(BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0.0,
                    FinanceHealth.INCOMPLETE,
                    "Vui lòng cập nhật thu nhập để tính toán DTI");
        }
        BigDecimal monthlyPayment = debtRepository.getTotalActiveExpectedMonthlyPayment(user.getId());
        if (monthlyPayment == null) monthlyPayment = BigDecimal.ZERO;

        double ratio = monthlyPayment.divide(income, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        FinanceHealth health = FinanceClassifier.byRatio(ratio, 30, 50);
        return new DtiResponse(income, monthlyPayment, ratio,health,  health.getDefaultAdvice());
    }
    // ratio = monthlyInterest / monthlyIncome
    private InterestRatioResponse calculateInterestRatio(User user, FinanceProfile financeProfile) {
        BigDecimal monthlyIncome = financeProfile.getMonthlyIncome();
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return new InterestRatioResponse(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO, 0.0,
                    FinanceHealth.INCOMPLETE,
                    "Vui lòng cập nhật thu nhập để tính toán !");}

        BigDecimal monthlyInterest = calculateTotalMonthlyInterest(user);
        double ratio = monthlyInterest.divide(monthlyIncome, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
        //lãi < 10% là GOOD, < 20% là WARNING, còn lại là CRITICAL
        FinanceHealth health = FinanceClassifier.byRatio(ratio, 10.0, 20.0);
        return new InterestRatioResponse(monthlyInterest, monthlyIncome, ratio, health, health.getDefaultAdvice());
    }
    // overdueDebts/ totalDebts
    private OverdueRatioResponse calculateOverdueRatio(User user){
        int overdueDebts = debtRepository.countDebtByStatus(user.getId(), DebtStatus.OVERDUE);
        int totalDebts = debtRepository.countDebtByStatus(user.getId(), DebtStatus.ACTIVE) + overdueDebts;
        if (totalDebts == 0) {
            return new OverdueRatioResponse(
                    0, 0, 0.0,
                    FinanceHealth.GOOD, "Không có khoản nợ đang hoạt động");}

        double ratio = ((double) overdueDebts / totalDebts) * 100;
        FinanceHealth health = FinanceClassifier.byRatio(ratio, 30, 50 );
        return new OverdueRatioResponse(totalDebts, overdueDebts, ratio, health, health.getDefaultAdvice());
    }
    private RepaymentTimeResponse calculateRepaymentTime(User user) {
        Long userId = user.getId();
        int activeDebtCount = debtRepository.countDebtByStatus(userId, DebtStatus.ACTIVE);
        if (activeDebtCount == 0) {
            return new RepaymentTimeResponse(0, 0);}
        BigDecimal totalRemaining = debtRepository.getTotalRemainingDebt(userId);
        if (totalRemaining == null) {
            totalRemaining = BigDecimal.ZERO;}

        if (totalRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            return new RepaymentTimeResponse(activeDebtCount, 0);}

        BigDecimal monthlyPayment = debtRepository.getTotalActiveExpectedMonthlyPayment(userId);
        if (monthlyPayment == null
                || monthlyPayment.compareTo(BigDecimal.ZERO) <= 0) {
            // không có khoản thanh toán hàng tháng
            return new RepaymentTimeResponse(activeDebtCount, null);
        }

        int estimatedMonths = totalRemaining
                .divide(monthlyPayment, 0, RoundingMode.CEILING)
                .intValue();
        return new RepaymentTimeResponse(activeDebtCount, estimatedMonths);
    }
    private BigDecimal calculateTotalMonthlyInterest(User user) {
        List<Debt> debts = debtRepository.findByUserIdAndStatusAndDeletedFalse(user.getId(), DebtStatus.ACTIVE);
        BigDecimal totalMonthlyInterest = BigDecimal.ZERO;

        for (Debt debt : debts) {
            BigDecimal monthlyInterest = interestCalculationService.calculateEstimatedMonthlyInterest(debt);
            if (monthlyInterest != null) {
                totalMonthlyInterest = totalMonthlyInterest.add(monthlyInterest);
            }
        }
        return totalMonthlyInterest;
    }


}