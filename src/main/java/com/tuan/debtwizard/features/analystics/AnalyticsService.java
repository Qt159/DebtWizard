/*package com.tuan.debtmanagement.service;

import com.tuan.debtmanagement.dto.DtiResponse;
import com.tuan.debtmanagement.exception.AppException;
import com.tuan.debtmanagement.exception.ErrorCode;
import com.tuan.debtmanagement.features.auth.model.User;
import com.tuan.debtmanagement.features.analystic.FinanceHealth;
import com.tuan.debtmanagement.repository.RepaymentScheduleRepository;
import com.tuan.debtmanagement.features.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

//@Service
public class AnalyticsService {

    private final RepaymentScheduleRepository scheduleRepo;
    private final UserRepository userRepo;

    public AnalyticsService(RepaymentScheduleRepository scheduleRepo, UserRepository userRepo) {
        this.scheduleRepo = scheduleRepo;
        this.userRepo = userRepo;
    }

    public DtiResponse calculateCurrentDti(Long userId, int month, int year) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        BigDecimal income = user.getMonthlyIncome();

        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return new DtiResponse(0, BigDecimal.ZERO, BigDecimal.ZERO,
                    FinanceHealth.CAUTION, "Vui lòng cập nhật thu nhập để tính toán DTI");
        }

        BigDecimal totalDebt = scheduleRepo.sumPendingAmountByMonth(userId, month, year);
        if (totalDebt == null) totalDebt = BigDecimal.ZERO;

        double ratio = totalDebt.divide(income, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .doubleValue();

        FinanceHealth health;
        if (ratio <= 30) health = FinanceHealth.EXCELLENT;
        else if (ratio <= 50) health = FinanceHealth.CAUTION;
        else health = FinanceHealth.CRITICAL;

        return new DtiResponse(ratio, totalDebt, income, health, health.getDefaultAdvice());
    }
}*/