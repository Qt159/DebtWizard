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
    public AnalysisResponse calculateAllAnalysis(Long userId){
        DtiResponse dti = calculateCurrentDti(userId);
        InterestRatioResponse interestRatio = calculateInterestRatio(userId);
        OverdueRatioResponse overdueRatio = calculateOverdueRatio(userId);
        RepaymentTimeResponse repaymentTime = calculateRepaymentTime(userId);
        return new AnalysisResponse(dti, interestRatio, overdueRatio, repaymentTime);
    }
    //monthlyPayment / income
    private DtiResponse calculateCurrentDti(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        BigDecimal income = user.getMonthlyIncome();
        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return new DtiResponse(BigDecimal.ZERO,
                    BigDecimal.ZERO, 0.0,
                    FinanceHealth.CRITICAL, "Vui lòng cập nhật thu nhập để tính toán DTI");
        }
        BigDecimal monthlyPayment = debtRepository.getTotalActiveExpectedMonthlyPayment(userId);
        if (monthlyPayment == null) monthlyPayment = BigDecimal.ZERO;
        double ratio = monthlyPayment.divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        FinanceHealth health = FinanceClassifier.byRatio(ratio, 30, 50, false);
        return new DtiResponse(income, monthlyPayment, ratio,health,  health.getDefaultAdvice());
    }
    // ratio = income / totalInterest
    // thu nhập hơn tiền lãi bao nhiêu lần -> đánh giá xem có đủ chi trả
    private InterestRatioResponse calculateInterestRatio(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        BigDecimal totalInterest = debtRepository.getTotalAccruedInterest(userId);
        BigDecimal totalPrincipal = debtRepository.getTotalDebt(userId);
        BigDecimal income = user.getMonthlyIncome();

        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return new InterestRatioResponse(BigDecimal.ZERO,
                    BigDecimal.ZERO, 0.0,
                    FinanceHealth.CRITICAL, "Vui lòng cập nhật thu nhập để tính toán !");
        }
        double ratio =income.divide(totalInterest, 4, RoundingMode.HALF_UP)
                .doubleValue();
        FinanceHealth health = FinanceClassifier.byRatio(ratio, 3.0, 1.5, true);
        return new InterestRatioResponse(totalPrincipal, totalInterest, ratio, health, health.getDefaultAdvice());
    }
    // overdueDebts/ totalActiveDebt
    private OverdueRatioResponse calculateOverdueRatio(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        int totalActiveDebts = debtRepository.countDebtByStatus(userId, DebtStatus.ACTIVE);
        int overdueDebts = debtRepository.countDebtByStatus(userId, DebtStatus.OVERDUE);
        double ratio = (double) overdueDebts / totalActiveDebts;
        FinanceHealth health = FinanceClassifier.byRatio(ratio, 0.3, 0.5, false);
        return new OverdueRatioResponse(totalActiveDebts, overdueDebts, ratio, health, health.getDefaultAdvice());
    }
    private RepaymentTimeResponse calculateRepaymentTime(Long userId){
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        BigDecimal totalRemaining = debtRepository.getTotalRemainingDebt(userId);
        BigDecimal monthlyExpectPayment = debtRepository.getTotalActiveExpectedMonthlyPayment(userId);
        int repaymentMonths = 0;
        if (totalRemaining != null && monthlyExpectPayment != null) {
            repaymentMonths = totalRemaining.divide(monthlyExpectPayment, 0, RoundingMode.CEILING).intValue();
        }
        // làm tròn lên
        return new RepaymentTimeResponse(debtRepository.countDebt(userId), repaymentMonths);


    }


}