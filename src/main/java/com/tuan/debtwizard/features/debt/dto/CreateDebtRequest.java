package com.tuan.debtwizard.features.debt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDebtRequest {

    @Valid
    @NotNull
    private DebtRequest debt;

    @Valid
    @NotNull
    private InterestSettingsRequest interestSettings;
}
