package com.tuan.debtwizard.features.planning.dto;

import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CompareRequest {
    @NotEmpty(message = "Danh sách khoản nợ không được để trống")
    @Size(max = 50, message = "Chỉ được so sánh tối đa 50 khoản nợ")

    private List<
    @Positive(message = "ID khoản nợ phải lớn hơn 0")
    Long> debtIds;

    @NotNull(message = "Số tiền trả thêm hàng tháng không được để trống")
    @PositiveOrZero(message = "Số tiền trả thêm hàng tháng phải lớn hơn hoặc bằng 0")
    private BigDecimal monthlyExtraPayment;

    @NotNull(message = "Vui lòng chọn chiến lược thứ nhất")
    private RepaymentStrategy firstStrategy;

   @NotNull(message = "Vui lòng chọn chiến lược thứ hai")
    private RepaymentStrategy secondStrategy;
}