package com.tuan.debtwizard.features.debt.mapper;

import com.tuan.debtwizard.features.debt.dto.*;
import com.tuan.debtwizard.features.debt.model.*;
import org.springframework.stereotype.Component;

@Component
public class DebtMapper {

    public Debt toEntity(CreateDebtRequest request, InterestSettingsRequest interestRequest) {

        Debt debt = new Debt();

        debt.setLenderName(request.getLenderName());
        debt.setTotalPrincipal(request.getTotalPrincipal());
        debt.setRemainingPrincipal(request.getTotalPrincipal());
        debt.setDebtType(request.getDebtType());
        debt.setTermMonths(request.getTermMonths());
        debt.setStartDate(request.getStartDate());
        debt.setDueDay(request.getDueDay());

        InterestSettings settings = new InterestSettings();
        settings.setInterestCalculationMethod(interestRequest.getInterestCalculationMethod());

        settings.setInterestFrequency(interestRequest.getInterestFrequency());

        settings.setInterestRate(interestRequest.getInterestRate());

        debt.setInterestSettings(settings);

        return debt;
    }


    public DebtResponse toResponse(Debt debt) {

        DebtResponse response = new DebtResponse();

        response.setId(debt.getId());
        response.setLenderName(debt.getLenderName());
        response.setTotalPrincipal(debt.getTotalPrincipal());
        response.setRemainingPrincipal(debt.getRemainingPrincipal());
        response.setExpectedMonthlyPayment(debt.getExpectedMonthlyPayment());
        response.setAccruedInterest(debt.getAccruedInterest());
        response.setTotalOutstanding(debt.getTotalOutstanding());
        response.setTermMonths(debt.getTermMonths());
        response.setStartDate(debt.getStartDate());
        response.setNextDueDate(debt.getNextDueDate());
        response.setLastPaymentDate(debt.getLastPaymentDate());
        response.setDueDay(debt.getDueDay());
        response.setStatus(debt.getStatus());
        response.setDebtType(debt.getDebtType());
        response.setCreatedAt(debt.getCreatedAt());
        response.setUpdatedAt(debt.getUpdatedAt());

        if (debt.getInterestSettings() != null) {
            response.setInterestRate(debt.getInterestSettings().getInterestRate());

            response.setInterestCalculationMethod(debt.getInterestSettings().getInterestCalculationMethod());

            response.setInterestFrequency(debt.getInterestSettings().getInterestFrequency());
        }

        return response;
    }


    public DebtListItemResponse toListItem(Debt debt) {

        DebtListItemResponse response = new DebtListItemResponse();

        response.setId(debt.getId());
        response.setLenderName(debt.getLenderName());
        response.setTotalPrincipal(debt.getTotalPrincipal());
        response.setRemainingPrincipal(debt.getRemainingPrincipal());
        response.setStatus(debt.getStatus());
        response.setDebtType(debt.getDebtType());
        response.setDueDay(debt.getDueDay());
        response.setNextDueDate(debt.getNextDueDate());
        if (debt.getInterestSettings() != null) {
            response.setInterestRate(debt.getInterestSettings().getInterestRate());
        }

        return response;
    }
}