package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.DebtType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateDebtRequest {

    @NotBlank(message = "Tên chủ nợ không được để trống")
    @Size(max = 100, message = "Tên chủ nợ tối đa 100 ký tự")
    private String lenderName;

    @NotNull(message = "Tổng nợ không được để trống")
    @Positive(message = "Tổng nợ không được nhỏ hơn 0")
    @Digits(integer = 15, fraction = 2, message = "Số tiền tối đa 15 chữ số và 2 chữ số thập phân")
    private BigDecimal totalPrincipal;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @PastOrPresent(message = "Ngày bắt đầu không được là ngày trong tương lai")
    private LocalDate startDate;

    @NotNull(message = "Thời hạn vay không được để trống")
    @Min(value = 1, message = "Thời hạn vay tối thiểu là 1 tháng")
    @Max(value = 600, message = "Thời hạn vay tối đa là 600 tháng")
    private Integer termMonths;

    @NotNull(message = "Ngày đến hạn không được để trống")
    @Min(1)
    @Max(31)
    private Integer dueDay;

    @NotNull(message = "Loại khoản vay không được để trống")
    private DebtType debtType;

    @Valid
    @NotNull(message = "Thông tin lãi suất không được để trống")
    private InterestSettingsRequest interestSettings;
}
