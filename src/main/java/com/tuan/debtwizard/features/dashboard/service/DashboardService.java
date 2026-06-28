package com.tuan.debtwizard.features.dashboard.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.dashboard.dto.DashboardResponse;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.payment.repository.PaymentRepository;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import com.tuan.debtwizard.features.dashboard.dto.NextDueDebtInfo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {
    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    public DashboardService(DebtRepository debtRepository, UserRepository userRepository, PaymentRepository paymentRepository) {
        this.debtRepository = debtRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UserDetails userDetails){
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Long userId = user.getId();
        DashboardResponse response = new DashboardResponse();
        response.setTotalDebt(debtRepository.getTotalDebt(userId));
        response.setTotalPaid(paymentRepository.getTotalPaid(userId));
        response.setRemainingDebt(debtRepository.getTotalRemainingDebt(userId));

        response.setActiveDebtCount(debtRepository.countDebtByStatus(userId, DebtStatus.ACTIVE));
        response.setPaidOffDebtCount(debtRepository.countDebtByStatus(userId, DebtStatus.PAID_OFF));
        response.setOverdueDebtCount(debtRepository.countDebtByStatus(userId, DebtStatus.OVERDUE));

        response.setOverdueAmount(debtRepository.getTotalOverdue(userId));
        response.setAccruedInterest(debtRepository.getTotalAccruedInterest(userId));

        response.setNextDueDebt(getNextDueDebt(user.getDebts()));
        response.setUpcomingDebts(getUpcomingDebts(user.getDebts()));
        return response;
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
                continue;}
            LocalDate dueDate = getNextDueDate(debt.getDueDay());
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
            if (daysUntilDue < minDays) {
                minDays = daysUntilDue;
                nextDebt = debt;
                nextDueDate = dueDate;
            }
        }

        if (nextDebt == null) {
            return null;}

        return new NextDueDebtInfo(
                nextDebt.getLenderName(),
                nextDueDate,
                minDays);
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
    private List<NextDueDebtInfo> getUpcomingDebts(List<Debt> debts) {
        LocalDate today = LocalDate.now();
        LocalDate limitDate = today.plusDays(7);
        List<NextDueDebtInfo> upcomingDebts = new ArrayList<>();
        for(Debt debt : debts){
            if(debt.getStatus() == DebtStatus.PAID_OFF) {
                continue;
            }
            LocalDate dueDate = getNextDueDate(debt.getDueDay());
            if(dueDate.isBefore(limitDate) &&dueDate.isAfter(today)){
                long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
                upcomingDebts.add(new NextDueDebtInfo(debt.getLenderName(), dueDate, daysUntilDue));
            }
        }
        return upcomingDebts;
    }
}
