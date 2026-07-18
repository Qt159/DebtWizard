package com.tuan.debtwizard.features.debt.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.dto.PagedResponse;
import com.tuan.debtwizard.features.debt.dto.CreateDebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtListItemResponse;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.dto.UpdateDebtRequest;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.model.DebtType;
import com.tuan.debtwizard.features.debt.service.DebtService;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/debts")
@SecurityRequirement(name = "Bearer Authentication")
public class DebtController {
    private final DebtService debtService;
    private final PaymentService paymentService;

    public DebtController(DebtService debtService, PaymentService paymentService) {
        this.debtService = debtService;
        this.paymentService = paymentService;
    }
    @PostMapping
    public ApiResponse<DebtResponse> createDebt(@Valid @RequestBody CreateDebtRequest createDebtRequest,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(debtService.createDebt(createDebtRequest, userDetails));
    }
    @GetMapping
    public ApiResponse<PagedResponse<DebtListItemResponse>> getDebts(
            @RequestParam(required = false) DebtStatus debtStatus,
            @RequestParam(required = false) DebtType debtType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateBefore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateAfter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.success(
                debtService.getDebts(userDetails, debtStatus, debtType, dueDateBefore, dueDateAfter, page, size, sortBy, sortDir));
    }
    @GetMapping("/{id}")
    public ApiResponse<DebtResponse> getDebtById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return ApiResponse.success(debtService.getDebtById(id, userDetails));
    }
    @PutMapping("/{id}")
    public ApiResponse<DebtResponse> updateDebt(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDebtRequest updateDebtRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ApiResponse.success(debtService.updateDebt(id, updateDebtRequest, userDetails));
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDebt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails){
            debtService.deleteDebt(id, userDetails);
            return ApiResponse.success();
    }
    @GetMapping("/{debtId}/payments")
    public ApiResponse<PagedResponse<PaymentListItem>> getPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long debtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.success(
                paymentService.getPayments(userDetails, debtId, page, size, sortBy, sortDir));
    }
}
