package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class DebtStateService {
    public void refreshDebtStatus(Debt debt){
        if(debt.getTotalOutstanding().compareTo(BigDecimal.ZERO) <= 0){
            debt.setStatus(DebtStatus.PAID_OFF);
            return;
        }
        if(LocalDate.now().isAfter(debt.getNextDueDate())){
            debt.setStatus(DebtStatus.OVERDUE);
            return;
        }
        debt.setStatus(DebtStatus.ACTIVE);
    }
    //dùng sau khi user thanh toán đủ một kỳ.
    public void moveNextDueDate(Debt debt, BigDecimal paymentAmount){
        if(paymentAmount.compareTo(debt.getExpectedMonthlyPayment()) >= 0){
            LocalDate nextMonth = debt.getNextDueDate().plusMonths(1);
            int correctDay = Math.min(debt.getDueDay(), nextMonth.lengthOfMonth());
            debt.setNextDueDate(nextMonth.withDayOfMonth(correctDay));
        }
    }

    //dùng khi tạo khoản nợ.
    //Nếu tạo trước ngày due của tháng đó -> Hạn trả là ngày due tháng đó.
    //Nếu tạo bằng hoặc sau ngày due của tháng đó -> Hạn trả dời sang ngày due tháng sau.
    public LocalDate calculateFirstDueDate(Debt debt) {
        LocalDate start = debt.getStartDate();
        if (start == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }

        Integer dueDay = debt.getDueDay();
        if (dueDay == null || dueDay < 1 || dueDay > 31) {
            throw new IllegalArgumentException("Due day must be between 1 and 31");
        }

        int dayOfMonth = Math.min(dueDay, start.lengthOfMonth());
        LocalDate firstDue = start.withDayOfMonth(dayOfMonth);
    //Nếu ngày tạo trùng hoặc đã vượt quá ngày due của tháng này -> Đẩy sang tháng sau
        if (!start.isBefore(firstDue)) {
            LocalDate nextMonth = start.plusMonths(1);
            dayOfMonth = Math.min(dueDay, nextMonth.lengthOfMonth());
            firstDue = nextMonth.withDayOfMonth(dayOfMonth);
        }
        
        return firstDue;
    }


}