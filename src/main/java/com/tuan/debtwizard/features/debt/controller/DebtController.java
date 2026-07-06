package com.tuan.debtwizard.features.debt.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.debt.dto.CreateDebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtListItemResponse;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.dto.DebtRequest;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.service.DebtService;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
    public ApiResponse<List<DebtListItemResponse>> getDebts(
            @RequestParam(required = false) DebtStatus debtStatus,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.success(debtService.getDebts(userDetails, debtStatus));
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
            @Valid @RequestBody DebtRequest debtRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ApiResponse.success(debtService.updateDebt(id, debtRequest, userDetails));
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDebt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails){
            debtService.deleteDebt(id, userDetails);
            return ApiResponse.success();
    }
    @GetMapping("/{debtId}/payments")
    public ApiResponse<List<PaymentListItem>> getPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long debtId
    ){
        return ApiResponse.success(paymentService.getPayments(userDetails, debtId));
    }
}
