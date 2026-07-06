package com.tuan.debtwizard.features.planning.dto;

import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SavePlanRequest {

    @NotEmpty(message = "Danh sách khoản nợ không được để trống")
    private List<Long> debtIds;

    @NotNull(message = "Số tiền trả thêm hàng tháng không được để trống")
    @PositiveOrZero(message = "Số tiền trả thêm phải >= 0")
    private BigDecimal monthlyExtraPayment;

    @NotNull(message = "Chiến lược không được để trống")
    private RepaymentStrategy strategy;
}
