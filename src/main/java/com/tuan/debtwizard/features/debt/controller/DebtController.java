package com.tuan.debtwizard.features.debt.controller;

import com.tuan.debtwizard.features.debt.dto.DebtListItem;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.dto.DebtRequest;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.service.DebtService;
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

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }
    @PostMapping
    public DebtResponse createDebt(@Valid @RequestBody DebtRequest debtRequest,
                                   @AuthenticationPrincipal UserDetails userDetails) {

        return debtService.createDebt(debtRequest, userDetails);
    }
    @GetMapping
    public List<DebtListItem> getListDebts(
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
}
