package com.tuan.debtwizard.features.planning.mapper;

import com.tuan.debtwizard.features.planning.dto.DebtPaymentDetailDto;
import com.tuan.debtwizard.features.planning.dto.SavedPlanResponse;
import com.tuan.debtwizard.features.planning.dto.SimulationMonthDto;
import com.tuan.debtwizard.features.planning.model.PlanDebtPayment;
import com.tuan.debtwizard.features.planning.model.PlanMonthlySchedule;
import com.tuan.debtwizard.features.planning.model.SavedPlan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SavedPlanMapper {
    public SavedPlanResponse toResponse(SavedPlan plan) {
        SavedPlanResponse response = new SavedPlanResponse();
        response.setId(plan.getId());
        response.setStrategy(plan.getStrategy());
        response.setPlanName(plan.getPlanName());
        response.setMonthlyExtraPayment(plan.getMonthlyExtraPayment());
        response.setTotalInterestPaid(plan.getTotalInterestPaid());
        response.setPayoffDurationMonths(plan.getPayoffDurationMonths());
        response.setSavedAt(plan.getSavedAt());
        response.setSchedule(toScheduleDtos(plan.getMonthlySchedules()));
        return response;
    }
    private List<SimulationMonthDto> toScheduleDtos(List<PlanMonthlySchedule> schedules) {
        List<SimulationMonthDto> result = new ArrayList<>();
        for (PlanMonthlySchedule schedule : schedules) {
            result.add(toScheduleDto(schedule));
        }
        return result;
    }

    private SimulationMonthDto toScheduleDto(PlanMonthlySchedule schedule) {
        SimulationMonthDto dto = new SimulationMonthDto();

        dto.setMonthIndex(schedule.getMonthIndex());
        dto.setDate(schedule.getDate());
        dto.setTotalPayment(schedule.getTotalPayment());
        dto.setExtraPaymentUsed(schedule.getExtraPaymentUsed());
        dto.setCashflowReleased(schedule.getCashflowReleased());
        dto.setPayments(toDebtPaymentDtos(schedule.getDebtPayments()));

        return dto;
    }

    private List<DebtPaymentDetailDto> toDebtPaymentDtos(List<PlanDebtPayment> payments) {
        List<DebtPaymentDetailDto> result = new ArrayList<>();

        for (PlanDebtPayment payment : payments) {
            result.add(toDebtPaymentDto(payment));
        }
        return result;
    }

    private DebtPaymentDetailDto toDebtPaymentDto(PlanDebtPayment payment) {
        DebtPaymentDetailDto dto = new DebtPaymentDetailDto();

        dto.setDebtId(payment.getDebt().getId());
        dto.setDebtName(payment.getDebtName());
        dto.setMinimumPaid(payment.getMinimumPaid());
        dto.setExtraPaid(payment.getExtraPaid());
        dto.setPrincipalPaid(payment.getPrincipalPaid());
        dto.setInterestPaid(payment.getInterestPaid());
        dto.setTotalPaid(payment.getMinimumPaid().add(payment.getExtraPaid()));
        dto.setRemainingBalance(payment.getRemainingBalance());
        dto.setPaidOff(payment.isPaidOff());

        return dto;
    }
}