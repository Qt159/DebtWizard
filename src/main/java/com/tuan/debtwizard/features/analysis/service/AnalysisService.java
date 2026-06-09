package com.tuan.debtwizard.features.analysis.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.analysis.FinanceHealth;
import com.tuan.debtwizard.features.analysis.dto.DtiResponse;
import com.tuan.debtwizard.features.auth.model.User;
import com.tuan.debtwizard.features.auth.repository.UserRepository;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.debt.service.DebtService;
import com.tuan.debtwizard.features.payment.repository.PaymentRepository;
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

    public DtiResponse calculateCurrentDti(Long userId) {
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

        FinanceHealth health;
        if (ratio <= 30) health = FinanceHealth.GOOD;
        else if (ratio <= 50) health = FinanceHealth.WARNING;
        else health = FinanceHealth.CRITICAL;
        return new DtiResponse(income, monthlyPayment, ratio,health,  health.getDefaultAdvice());
    }
}