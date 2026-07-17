package com.tuan.debtwizard.features.payment.mapper;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.dto.PaymentRequest;
import com.tuan.debtwizard.features.payment.dto.PaymentResponse;
import com.tuan.debtwizard.features.payment.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toEntity(PaymentRequest paymentRequest, Debt debt) {
        Payment payment = new Payment();

        payment.setDebt(debt);
        payment.setAmount(paymentRequest.getAmount());
        payment.setPaymentDate(paymentRequest.getPaymentDate());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setNote(paymentRequest.getNote());
        return payment;
    }


    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        Debt debt = payment.getDebt();
        if (debt != null) {
            response.setDebtId(debt.getId());
            response.setLenderName(debt.getLenderName());
        }
        response.setNote(payment.getNote());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setPaymentDate(payment.getPaymentDate());
        response.setInterestPaid(payment.getInterestPaid());
        response.setPrincipalPaid(payment.getPrincipalPaid());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
    public PaymentListItem toListItem(Payment payment) {
        PaymentListItem item = new PaymentListItem();
        Debt debt = payment.getDebt();
        item.setId(payment.getId());
        if (debt != null) {
            item.setDebtId(debt.getId());}

        item.setAmount(payment.getAmount());
        item.setPaymentMethod(payment.getPaymentMethod());
        item.setNote(payment.getNote());
        item.setPaymentDate(payment.getPaymentDate());
        return item;
    }
}