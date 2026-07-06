package com.tuan.debtwizard.features.payment.controller;

import com.tuan.debtwizard.dto.ApiResponse;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.dto.PaymentRequest;
import com.tuan.debtwizard.features.payment.dto.PaymentResponse;
import com.tuan.debtwizard.features.payment.dto.UpdatePaymentRequest;
import com.tuan.debtwizard.features.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@SecurityRequirement(name = "Bearer Authentication")
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

    @GetMapping
    public ApiResponse<List<PaymentListItem>> getAllPayments(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(paymentService.getAllPayments(userDetails));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ApiResponse.success(paymentService.getPayment(userDetails, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<PaymentResponse> updatePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentRequest request) {
        return ApiResponse.success(paymentService.updatePayment(id, request, userDetails));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        paymentService.deletePayment(id, userDetails);
        return ApiResponse.success();
    }
}
