package com.tuan.debtwizard.features.analysis.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.analysis.FinanceHealth;
import com.tuan.debtwizard.features.analysis.dto.*;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
public class AnalysisService {

    private final UserRepository userRepo;
    private final DebtRepository debtRepository;

    public AnalysisService( UserRepository userRepo,
                           DebtRepository debtRepository) {
        this.userRepo = userRepo;
        this.debtRepository = debtRepository;
    }
    public AnalysisResponse calculateAllAnalysis(String username){
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        DtiResponse dti = calculateCurrentDti(user);
        InterestRatioResponse interestRatio = calculateInterestRatio(user);
        OverdueRatioResponse overdueRatio = calculateOverdueRatio(user);
        RepaymentTimeResponse repaymentTime = calculateRepaymentTime(user);
        return new AnalysisResponse(dti, interestRatio, overdueRatio, repaymentTime);
    }
    //monthlyPayment / income
    private DtiResponse calculateCurrentDti(User user) {
        BigDecimal income = user.getMonthlyIncome();
        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return new DtiResponse(BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0.0,
                    FinanceHealth.CRITICAL,
                    "Vui lòng cập nhật thu nhập để tính toán DTI");
        }
        BigDecimal monthlyPayment = debtRepository.getTotalActiveExpectedMonthlyPayment(user.getId());
        if (monthlyPayment == null) monthlyPayment = BigDecimal.ZERO;

        double ratio = monthlyPayment.divide(income, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        FinanceHealth health = FinanceClassifier.byRatio(ratio, 30, 50);
        return new DtiResponse(income, monthlyPayment, ratio,health,  health.getDefaultAdvice());
    }
    // ratio = income / totalInterest
    private InterestRatioResponse calculateInterestRatio(User user){
        BigDecimal income = user.getMonthlyIncome();
        BigDecimal totalInterest = debtRepository.getTotalAccruedInterest(user.getId());
        BigDecimal totalPrincipal = debtRepository.getTotalDebt(user.getId());

        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return new InterestRatioResponse(BigDecimal.ZERO,
                    BigDecimal.ZERO, 0.0,
                    FinanceHealth.CRITICAL, "Vui lòng cập nhật thu nhập để tính toán !");}

        if (totalInterest == null || totalInterest.compareTo(BigDecimal.ZERO) <= 0) {
            return new InterestRatioResponse(
                    totalPrincipal == null ? BigDecimal.ZERO : totalPrincipal,
                    BigDecimal.ZERO, 0.0,
                    FinanceHealth.GOOD, "Không có tiền lãi phải trả");}

        double ratio = totalInterest.divide(income, 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).doubleValue();
        //lãi < 10% là GOOD, < 20% là WARNING, còn lại là CRITICAL
        FinanceHealth health = FinanceClassifier.byRatio(ratio, 10.0, 20.0);
        return new InterestRatioResponse(totalPrincipal, totalInterest, ratio, health, health.getDefaultAdvice());
    }
    // overdueDebts/ totalActiveDebt
    private OverdueRatioResponse calculateOverdueRatio(User user){
        int totalActiveDebts = debtRepository.countDebtByStatus(user.getId(), DebtStatus.ACTIVE);
        int overdueDebts = debtRepository.countDebtByStatus(user.getId(), DebtStatus.OVERDUE);
        if (totalActiveDebts == 0) {
            return new OverdueRatioResponse(
                    0, 0, 0.0,
                    FinanceHealth.GOOD, "Không có khoản nợ đang hoạt động");}

        double ratio = ((double) overdueDebts / totalActiveDebts) * 100;
        FinanceHealth health = FinanceClassifier.byRatio(ratio, 30, 50 );
        return new OverdueRatioResponse(totalActiveDebts, overdueDebts, ratio, health, health.getDefaultAdvice());
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
            return new RepaymentTimeResponse(activeDebtCount, -1);
        }

        int estimatedMonths = totalRemaining
                .divide(monthlyPayment, 0, RoundingMode.CEILING)
                .intValue();
        return new RepaymentTimeResponse(activeDebtCount, estimatedMonths);
    }


}