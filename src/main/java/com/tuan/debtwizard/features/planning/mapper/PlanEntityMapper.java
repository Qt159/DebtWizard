package com.tuan.debtwizard.features.planning.mapper;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.planning.dto.DebtPaymentDetailDto;
import com.tuan.debtwizard.features.planning.dto.SimulationMonthDto;
import com.tuan.debtwizard.features.planning.model.PlanDebtPayment;
import com.tuan.debtwizard.features.planning.model.PlanMonthlySchedule;
import com.tuan.debtwizard.features.planning.model.SavedPlan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlanEntityMapper {
    public List<PlanMonthlySchedule> toSchedules(SavedPlan plan, List<SimulationMonthDto> months, List<Debt> debts) {

        List<PlanMonthlySchedule> schedules = new ArrayList<>();
        for (SimulationMonthDto month : months) {
            PlanMonthlySchedule schedule = new PlanMonthlySchedule();

            schedule.setSavedPlan(plan);
            schedule.setMonthIndex(month.getMonthIndex());
            schedule.setDate(month.getDate());
            schedule.setTotalPayment(month.getTotalPayment());
            schedule.setExtraPaymentUsed(month.getExtraPaymentUsed());
            schedule.setCashflowReleased(month.getCashflowReleased());
            schedule.setDebtPayments(toDebtPayments(schedule, month.getPayments(), debts));

            schedules.add(schedule);
        }
        return schedules;
    }

    private List<PlanDebtPayment> toDebtPayments(
            PlanMonthlySchedule schedule,
            List<DebtPaymentDetailDto> payments,
            List<Debt> debts) {

        List<PlanDebtPayment> result = new ArrayList<>();
        for (DebtPaymentDetailDto payment : payments) {
            Debt debt = findDebt(payment.getDebtId(), debts);
            PlanDebtPayment entity = new PlanDebtPayment();
            entity.setSchedule(schedule);
            entity.setDebt(debt);
            entity.setDebtName(payment.getDebtName());
            entity.setMinimumPaid(payment.getMinimumPaid());
            entity.setExtraPaid(payment.getExtraPaid());
            entity.setPrincipalPaid(payment.getPrincipalPaid());
            entity.setInterestPaid(payment.getInterestPaid());
            entity.setRemainingBalance(payment.getRemainingBalance());
            entity.setPaidOff(payment.isPaidOff());
            result.add(entity);
        }
        return result;
    }

    private Debt findDebt(Long debtId, List<Debt> debts) {
        for (Debt debt : debts) {
            if (debt.getId().equals(debtId)) {
                return debt;
            }
        }
        throw new AppException(ErrorCode.DEBT_NOT_FOUND);
    }
}