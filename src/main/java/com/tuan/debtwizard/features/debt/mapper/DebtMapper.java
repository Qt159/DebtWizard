package com.tuan.debtwizard.features.debt.mapper;

import com.tuan.debtwizard.features.debt.dto.DebtListItemResponse;
import com.tuan.debtwizard.features.debt.dto.DebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.dto.InterestSettingsRequest;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.InterestSettings;
import org.springframework.stereotype.Component;

@Component
public class DebtMapper {

    public Debt toEntity(DebtRequest debtRequest, InterestSettingsRequest interestSettingsRequest) {
        Debt debt = new Debt();
        debt.setLenderName(debtRequest.getLenderName());
        debt.setTotalPrincipal(debtRequest.getTotalPrincipal());
        debt.setRemainingPrincipal(debtRequest.getTotalPrincipal());
        debt.setDebtType(debtRequest.getDebtType());
        debt.setTermMonths(debtRequest.getTermMonths());
        debt.setStartDate(debtRequest.getStartDate());
        debt.setDueDay(debtRequest.getDueDay());

        InterestSettings settings = new InterestSettings();
        settings.setInterestCalculationMethod(interestSettingsRequest.getInterestCalculationMethod());
        settings.setInterestFrequency(interestSettingsRequest.getInterestFrequency());
        settings.setInterestRate(interestSettingsRequest.getInterestRate());

        debt.setInterestSettings(settings);

        return debt;
    }

    public DebtResponse toResponse(Debt debt) {
        DebtResponse response = new DebtResponse();
        response.setId(debt.getId());
        response.setLenderName(debt.getLenderName());
        response.setTotalPrincipal(debt.getTotalPrincipal());
        response.setRemainingPrincipal(debt.getRemainingPrincipal());
        response.setAccruedInterest(debt.getAccruedInterest());
        response.setTotalOutstanding(debt.getTotalOutstanding());
        response.setExpectedMonthlyPayment(debt.getExpectedMonthlyPayment());
        response.setTermMonths(debt.getTermMonths());
        response.setStartDate(debt.getStartDate());
        response.setNextDueDate(debt.getNextDueDate());
        response.setLastPaymentDate(debt.getLastPaymentDate());
        response.setDueDay(debt.getDueDay());
        response.setStatus(debt.getStatus());
        response.setDebtType(debt.getDebtType());


        if (debt.getInterestSettings() != null) {
            response.setInterestRate(debt.getInterestSettings().getInterestRate());
            response.setInterestCalculationMethod(debt.getInterestSettings().getInterestCalculationMethod());
            response.setInterestFrequency(debt.getInterestSettings().getInterestFrequency());
        }

        response.setCreatedAt(debt.getCreatedAt());
        response.setUpdatedAt(debt.getUpdatedAt());
        return response;
    }

    public DebtListItemResponse toListItem(Debt debt) {
        DebtListItemResponse item = new DebtListItemResponse();
        item.setId(debt.getId());
        item.setLenderName(debt.getLenderName());
        item.setRemainingPrincipal(debt.getRemainingPrincipal());
        item.setTotalPrincipal(debt.getTotalPrincipal());
        item.setDueDay(debt.getDueDay());
        item.setStatus(debt.getStatus());
        return item;
    }
}
