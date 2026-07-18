package com.tuan.debtwizard.features.dashboard.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.dashboard.dto.DashboardResponse;
import com.tuan.debtwizard.features.dashboard.dto.NextDueDebtInfo;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.payment.repository.PaymentRepository;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DashboardService {

    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(DebtRepository debtRepository,
                            UserRepository userRepository,
                            PaymentRepository paymentRepository) {
        this.debtRepository = debtRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Long userId = user.getId();

        List<Debt> activeDebts = debtRepository. findUnpaidDebtsByUserId(userId);

        DashboardResponse response = new DashboardResponse();

        response.setTotalDebt(getTotalDebt(userId));
        response.setTotalPaid(getTotalPaid(userId));
        response.setRemainingDebt(getRemainingDebt(userId));
        response.setActiveDebtCount(countDebtByStatus(userId, DebtStatus.ACTIVE));
        response.setPaidOffDebtCount(countDebtByStatus(userId, DebtStatus.PAID_OFF));
        response.setOverdueDebtCount(countDebtByStatus(userId, DebtStatus.OVERDUE));
        response.setOverdueAmount(getTotalOverdue(userId));
        response.setAccruedInterest(getAccruedInterest(userId));
        response.setNextDueDebt(getNextDueDebt(activeDebts));
        response.setUpcomingDebts(getUpcomingDebts(activeDebts));

        return response;
    }

    private BigDecimal getTotalDebt(Long userId) {
        BigDecimal totalDebt = debtRepository.getTotalDebt(userId);
        if (totalDebt == null) {
            totalDebt = BigDecimal.ZERO;}
        return totalDebt;
    }

    private BigDecimal getTotalPaid(Long userId) {
        BigDecimal totalPaid = paymentRepository.getTotalPaid(userId);
        if (totalPaid == null) {
            totalPaid = BigDecimal.ZERO;}
        return totalPaid;
    }

    private BigDecimal getRemainingDebt(Long userId) {
        BigDecimal remainingDebt = debtRepository.getTotalRemainingDebt(userId);
        if (remainingDebt == null) {
            remainingDebt = BigDecimal.ZERO;
        }
        return remainingDebt;
    }

    private int countDebtByStatus(Long userId, DebtStatus status) {
        return debtRepository.countDebtByStatus(userId, status);
    }

    private BigDecimal getTotalOverdue(Long userId) {
        BigDecimal overdue = debtRepository.getTotalOverdue(userId);
        if (overdue == null) {
            overdue = BigDecimal.ZERO;
        }
        return overdue;
    }

    private BigDecimal getAccruedInterest(Long userId) {
        BigDecimal accruedInterest = debtRepository.getTotalAccruedInterest(userId);
        if (accruedInterest == null) {
            accruedInterest = BigDecimal.ZERO;}
        return accruedInterest;
    }

    private NextDueDebtInfo getNextDueDebt(List<Debt> debts) {
        Debt nextDebt = null;
        LocalDate nextDueDate = null;
        long minDays = Long.MAX_VALUE;
        for (Debt debt : debts) {
            LocalDate dueDate = getNextDueDate(debt.getDueDay());
            if (dueDate == null) {
                continue;
            }
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
            if (daysUntilDue < minDays) {
                minDays = daysUntilDue;
                nextDebt = debt;
                nextDueDate = dueDate;
            }}
        if (nextDebt == null) {return null;}
        return new NextDueDebtInfo(nextDebt.getLenderName(), nextDueDate, minDays);
    }

    private LocalDate getNextDueDate(Integer dueDay) {
        if (dueDay == null) {return null;}
        LocalDate today = LocalDate.now();
        LocalDate nextDueDate = today.withDayOfMonth(
                Math.min(dueDay, today.lengthOfMonth()));

        if (nextDueDate.isBefore(today)) {
            LocalDate nextMonth = today.plusMonths(1);
            nextDueDate = nextMonth.withDayOfMonth(
                    Math.min(dueDay, nextMonth.lengthOfMonth()));
        }
        return nextDueDate;
    }

    private List<NextDueDebtInfo> getUpcomingDebts(List<Debt> debts) {
        LocalDate today = LocalDate.now();
        LocalDate limitDate = today.plusDays(7);
        List<NextDueDebtInfo> upcomingDebts = new ArrayList<>();

        for (Debt debt : debts) {
            LocalDate dueDate = getNextDueDate(debt.getDueDay());
            if (dueDate == null) {
                continue;}

            if (!dueDate.isBefore(today) && !dueDate.isAfter(limitDate)) {
                long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
                upcomingDebts.add(new NextDueDebtInfo(debt.getLenderName(), dueDate, daysUntilDue));}
        }
        return upcomingDebts;
    }
}