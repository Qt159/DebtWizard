package com.tuan.debtwizard.features.summary.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.payment.repository.PaymentRepository;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import com.tuan.debtwizard.features.summary.dto.NextDueDebtInfo;
import com.tuan.debtwizard.features.summary.dto.SummaryResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        BigDecimal remainingDebt =
                debtRepository.getTotalRemainingDebt(userId);

        int overdueDebtCount = countDebtByStatus(userId, DebtStatus.OVERDUE);
        BigDecimal overdueAmount = getTotalOverdue(userId);
        int paidOffDebtCount = countDebtByStatus(userId, DebtStatus.PAID_OFF);
        int activeDebtCount = countDebtByStatus(userId, DebtStatus.ACTIVE);
        BigDecimal accruedInterest = getAccruedInterest(userId);
        NextDueDebtInfo nextDueDebtInfo = getNextDueDebt(user.getDebts());
        return new SummaryResponse(
            totalDebt, totalPaid, remainingDebt, activeDebtCount, paidOffDebtCount,
                overdueDebtCount, overdueAmount, accruedInterest, nextDueDebtInfo);
    }

    private BigDecimal getTotalOverdue(Long userId) {
        BigDecimal overdue = debtRepository.getTotalOverdue(userId);
        if(overdue == null){
            overdue = BigDecimal.ZERO;
        }
        return overdue;
    }
    private BigDecimal getAccruedInterest(Long userId) {
        BigDecimal accruedInterest = debtRepository.getTotalAccruedInterest(userId);
        if(accruedInterest == null){
            accruedInterest = BigDecimal.ZERO;
        }
        return accruedInterest;
    }
    private NextDueDebtInfo getNextDueDebt(List<Debt> debts) {
        Debt nextDebt = null;
        LocalDate nextDueDate = null;
        long minDays = Long.MAX_VALUE;
        for (Debt debt : debts) {
            if (debt.getStatus() == DebtStatus.PAID_OFF) {
                continue;
            }
            LocalDate dueDate = getNextDueDate(debt.getDueDay());
            long daysUntilDue =
                    ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            dueDate
                    );
            if (daysUntilDue < minDays) {
                minDays = daysUntilDue;
                nextDebt = debt;
                nextDueDate = dueDate;
            }
        }

        if (nextDebt == null) {
            return null;
        }

        return new NextDueDebtInfo(
                nextDebt.getLenderName(),
                nextDueDate,
                minDays
        );
    }
    private LocalDate getNextDueDate(Integer dueDay) {
        LocalDate today = LocalDate.now();
        LocalDate nextDueDate = today.withDayOfMonth(
                Math.min(dueDay, today.lengthOfMonth())
        );
        if (nextDueDate.isBefore(today)) {
            LocalDate nextMonth = today.plusMonths(1);
            nextDueDate = nextMonth.withDayOfMonth(
                    Math.min(dueDay, nextMonth.lengthOfMonth())
            );
        }
        return nextDueDate;
    }

}
