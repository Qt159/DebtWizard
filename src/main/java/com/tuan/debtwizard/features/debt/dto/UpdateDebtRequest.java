package com.tuan.debtwizard.features.debt.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDebtRequest {
    @NotBlank
    @Size(max = 100)
    private String lenderName;

}

