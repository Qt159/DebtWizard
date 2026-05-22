package com.tuan.debtwizard.features.summary.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.auth.model.User;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.payment.repository.PaymentRepository;
import com.tuan.debtwizard.features.auth.repository.UserRepository;
import com.tuan.debtwizard.features.summary.dto.SummaryResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SummaryService {
    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    public SummaryService(DebtRepository debtRepository, UserRepository userRepository, PaymentRepository paymentRepository) {
        this.debtRepository = debtRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }
    protected BigDecimal getTotalDebt(Long userId) {
        BigDecimal totalDebt = debtRepository.getTotalDebt(userId);
        if(totalDebt == null){ totalDebt = BigDecimal.ZERO;}
        return totalDebt;
    }
    protected  BigDecimal getTotalPaid(Long userId) {
        BigDecimal totalPaid = paymentRepository.getTotalPaid(userId);
        if(totalPaid == null){ totalPaid = BigDecimal.ZERO;}
        return totalPaid;
    }
    protected int countDebt(Long userId) {
        return debtRepository.countDebt(userId);

    }
    protected int countDebtByStatus(Long userId, DebtStatus status) {
        return debtRepository.countDebtByStatus(userId, status);
    }
    @Transactional(readOnly = true)
    public SummaryResponse getSummary(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Long userId = user.getId();
        BigDecimal totalDebt = getTotalDebt(userId);
        BigDecimal totalPaid = getTotalPaid(userId);
        BigDecimal remaining = totalDebt.subtract(totalPaid);
        int debtCount = countDebt(userId);
        int overdueDebtCount = countDebtByStatus(userId, DebtStatus.OVERDUE);
        BigDecimal overdueAmount = getTotalOverdue(userId);
        int paidOffDebtCount = countDebtByStatus(userId, DebtStatus.PAID_OFF);
        return new SummaryResponse(
            totalDebt, totalPaid, remaining, debtCount, overdueDebtCount, overdueAmount, paidOffDebtCount);
    }

    private BigDecimal getTotalOverdue(Long userId) {
        BigDecimal overdue = debtRepository.getTotalOverdue(userId);
        if(overdue == null){
            overdue = BigDecimal.ZERO;
        }
        return overdue;
    }


}
