package com.tuan.debtwizard.features.payment.controller;


import com.tuan.debtwizard.dto.ApiResponse;
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
    public ApiResponse<PaymentResponse> createPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentRequest paymentRequest) {
        return ApiResponse.success(paymentService.createPayment(paymentRequest, userDetails));
    }
    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id){
        return ApiResponse.success(paymentService.getPayment(userDetails, id));
    }

}
