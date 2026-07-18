package com.tuan.debtwizard.features.planning.mapper;

import com.tuan.debtwizard.features.planning.dto.DebtPaymentDetailDto;
import com.tuan.debtwizard.features.planning.dto.PlanComparisonDto;
import com.tuan.debtwizard.features.planning.dto.SimulationMonthDto;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class SimulationMapper {

    public List<DebtPaymentDetailDto> toPaymentDetails(List<DebtSnapshot> debts) {
        List<DebtPaymentDetailDto> result = new ArrayList<>();
        for (DebtSnapshot debt : debts) {
            DebtPaymentDetailDto dto = new DebtPaymentDetailDto();

            dto.setDebtId(debt.getDebtId());
            dto.setDebtName(debt.getDebtName());
            dto.setMinimumPaid(debt.getCurrentMinimumPaid());
            dto.setExtraPaid(debt.getCurrentExtraPaid());
            dto.setPrincipalPaid(debt.getCurrentPrincipalPaid());
            dto.setInterestPaid(debt.getCurrentInterestPaid());
            dto.setTotalPaid(debt.getPaymentThisMonth());
            dto.setRemainingBalance(debt.getBalance());
            dto.setPaidOff(debt.isPaidOff());
            result.add(dto);
        }

        return result;
    }

    public SimulationMonthDto toSimulationMonth(
            int monthIndex,
            BigDecimal totalPayment,
            BigDecimal extraPaymentUsed,
            BigDecimal cashflowReleased,
            List<DebtPaymentDetailDto> payments) {

        SimulationMonthDto dto = new SimulationMonthDto();

        dto.setMonthIndex(monthIndex);
        dto.setDate(LocalDate.now().plusMonths(monthIndex));
        dto.setTotalPayment(totalPayment);
        dto.setExtraPaymentUsed(extraPaymentUsed);
        dto.setCashflowReleased(cashflowReleased);
        dto.setPayments(payments);

        return dto;
    }

    public PlanComparisonDto toPlanComparison(
            RepaymentStrategy strategy,
            BigDecimal totalInterest,
            int payoffMonths,
            List<SimulationMonthDto> schedule) {

        PlanComparisonDto dto = new PlanComparisonDto();

        dto.setStrategy(strategy);
        dto.setPlanName(strategy.getDisplayName());
        dto.setTotalInterestPaid(totalInterest);
        dto.setPayoffDurationMonths(payoffMonths);
        dto.setSchedule(schedule);

        return dto;
    }
}