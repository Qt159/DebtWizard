package com.tuan.debtwizard.features.debt.controller;

import com.tuan.debtwizard.features.debt.dto.CreateDebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtListItemResponse;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.dto.DebtRequest;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.service.DebtService;
import com.tuan.debtwizard.features.interest.dto.InterestConfigRequest;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/debts")

public class DebtController {
    private final DebtService debtService;
    private final PaymentService paymentService;

    public DebtController(DebtService debtService, PaymentService paymentService) {
        this.debtService = debtService;
        this.paymentService = paymentService;
    }
    @PostMapping
    public DebtResponse createDebt(@Valid @RequestBody CreateDebtRequest createDebtRequest,
                                   @AuthenticationPrincipal UserDetails userDetails) {

        return debtService.createDebt(createDebtRequest, userDetails);
    }
    @GetMapping
    public List<DebtListItemResponse> getListDebts(
            @RequestParam(required = false) DebtStatus debtStatus,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return debtService.getDebts(userDetails, debtStatus);
    }
    @GetMapping("/{id}")
    public DebtResponse getDebtById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return debtService.getDebtById(id,userDetails);
    }
    @PutMapping("/{id}")
    public DebtResponse updateDebt(
            @PathVariable Long id,
            @Valid @RequestBody DebtRequest debtRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        return debtService.updateDebt(id, debtRequest, userDetails);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails){
            debtService.deleteDebt(id, userDetails);
            return ResponseEntity.noContent().build();
    }
    @GetMapping("/{debtId}/payments")
    public List<PaymentListItem> getPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long debtId
    ){
        return paymentService.getPayments(userDetails, debtId);
    }
}
