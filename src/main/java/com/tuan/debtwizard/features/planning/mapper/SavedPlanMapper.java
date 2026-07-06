package com.tuan.debtwizard.features.planning.mapper;

import com.tuan.debtwizard.features.planning.dto.DebtPaymentDetailDto;
import com.tuan.debtwizard.features.planning.dto.SavedPlanResponse;
import com.tuan.debtwizard.features.planning.dto.SimulationMonthDto;
import com.tuan.debtwizard.features.planning.model.PlanDebtPayment;
import com.tuan.debtwizard.features.planning.model.PlanMonthlySchedule;
import com.tuan.debtwizard.features.planning.model.SavedPlan;
import org.springframework.stereotype.Component;

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
        return schedules.stream()
                .map(this::toScheduleDto)
                .toList();
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
        return payments.stream()
                .map(this::toDebtPaymentDto)
                .toList();
    }

    private DebtPaymentDetailDto toDebtPaymentDto(PlanDebtPayment p) {
        DebtPaymentDetailDto dto = new DebtPaymentDetailDto();
        dto.setDebtId(p.getDebt().getId());
        dto.setDebtName(p.getDebtName());
        dto.setMinimumPaid(p.getMinimumPaid());
        dto.setExtraPaid(p.getExtraPaid());
        dto.setPrincipalPaid(p.getPrincipalPaid());
        dto.setInterestPaid(p.getInterestPaid());
        dto.setTotalPaid(p.getMinimumPaid().add(p.getExtraPaid()));
        dto.setRemainingBalance(p.getRemainingBalance());
        dto.setPaidOff(p.isPaidOff());
        return dto;
    }
}
