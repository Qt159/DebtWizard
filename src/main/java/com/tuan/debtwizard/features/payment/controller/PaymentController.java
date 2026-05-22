package com.tuan.debtwizard.features.payment.controller;


import com.tuan.debtwizard.features.payment.service.PaymentService;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.dto.PaymentRequest;
import com.tuan.debtwizard.features.payment.dto.PaymentResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @PostMapping
    public PaymentResponse createPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentRequest paymentRequest) {
        return  paymentService.createPayment(paymentRequest, userDetails);
    }
    @GetMapping("/{id}")
    public PaymentResponse getPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id){
        return paymentService.getPayment(userDetails,id);
    }
    @GetMapping()
    public List<PaymentListItem> getPayments(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam Long debtId
    ){
        return paymentService.getPayments(userDetails, debtId);
    }
}
