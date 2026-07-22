package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.InterestCalculationMethod;
import com.tuan.debtwizard.features.debt.model.InterestFrequency;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InterestSettingsRequest {
    @NotNull(message = "Phương pháp tính lãi không được để trống")
    private InterestCalculationMethod interestCalculationMethod;

    @NotNull(message = "Vui lòng chọn chu kỳ tính lãi (Hằng ngày, Hằng tháng hoặc Hằng năm)")
    private InterestFrequency interestFrequency;

    @NotNull(message = "Lãi suất không được để trống")
    @DecimalMin(value = "0.0", message = "Lãi suất phải lớn hơn hoặc bằng 0%")
    @DecimalMax(value = "100.0", message = "Lãi suất không được vượt quá 100%")
    private BigDecimal interestRate;
}
