package com.tuan.debtwizard.features.payment.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.service.DebtStateService;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.auth.model.User;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.auth.repository.UserRepository;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.dto.PaymentRequest;
import com.tuan.debtwizard.features.payment.dto.PaymentResponse;
import com.tuan.debtwizard.features.payment.mapper.PaymentMapper;
import com.tuan.debtwizard.features.payment.model.Payment;
import com.tuan.debtwizard.features.payment.repository.PaymentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final DebtStateService debtStateService;

    public PaymentService(PaymentRepository paymentRepository, DebtRepository debtRepository, PaymentMapper paymentMapper, UserRepository userRepository, DebtStateService debtStateService) {
        this.paymentRepository = paymentRepository;
        this.debtRepository = debtRepository;
        this.paymentMapper = paymentMapper;
        this.userRepository = userRepository;
        this.debtStateService = debtStateService;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Debt debt = debtRepository.findById(request.getDebtId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        if (debt.isDeleted()) {
            throw new AppException(ErrorCode.DEBT_DELETED);
        }
        Long ownerId = debt.getUser().getId();
        if (!ownerId.equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        if (debt.getStatus() == DebtStatus.PAID_OFF) {
            throw new AppException(ErrorCode.DEBT_ALREADY_PAID_OFF);
        }
        BigDecimal remaining = debt.getRemainingPrincipal();
        BigDecimal amount = request.getAmount();
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        if (amount.compareTo(remaining) > 0) {
            throw new AppException(ErrorCode.PAYMENT_EXCEEDS_REMAINING);
        }

        BigDecimal newRemaining = remaining.subtract(amount);
        debt.setRemainingPrincipal(newRemaining);
        debtStateService.refreshDebtStatus(debt);
        debtRepository.save(debt);
        Payment payment = paymentMapper.toEntity(request, debt);
        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UserDetails userDetails, Long id) {
        User user =  userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Long ownerId = payment.getDebt().getUser().getId();
        if (!ownerId.equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentListItem> getPayments(UserDetails userDetails, Long debtId) {
        User user =  userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        if (!debt.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        List<Payment> payments = paymentRepository.findByDebtId(debtId);

        List<PaymentListItem> items = new ArrayList<>();
        for(Payment payment :payments) {
            items.add(paymentMapper.toListItem(payment));
        }
        return items;
        }
    }
