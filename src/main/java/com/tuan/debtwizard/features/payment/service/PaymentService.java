package com.tuan.debtwizard.features.payment.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.debt.service.DebtStateService;
import com.tuan.debtwizard.features.debt.service.interest.InterestAccrualService;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.dto.PaymentRequest;
import com.tuan.debtwizard.features.payment.dto.PaymentResponse;
import com.tuan.debtwizard.features.payment.dto.UpdatePaymentRequest;
import com.tuan.debtwizard.features.payment.mapper.PaymentMapper;
import com.tuan.debtwizard.features.payment.model.Payment;
import com.tuan.debtwizard.features.payment.repository.PaymentRepository;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final DebtStateService debtStateService;
    private final InterestAccrualService interestAccrualService;

    public PaymentService(PaymentRepository paymentRepository,
                          DebtRepository debtRepository,
                          PaymentMapper paymentMapper,
                          UserRepository userRepository,
                          DebtStateService debtStateService,
                          InterestAccrualService interestAccrualService) {
        this.paymentRepository = paymentRepository;
        this.debtRepository = debtRepository;
        this.paymentMapper = paymentMapper;
        this.userRepository = userRepository;
        this.debtStateService = debtStateService;
        this.interestAccrualService = interestAccrualService;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Debt debt = debtRepository
                .findByIdAndUserIdAndDeletedFalse(request.getDebtId(), user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));

        if (debt.getStatus() == DebtStatus.PAID_OFF) {
            throw new AppException(ErrorCode.DEBT_ALREADY_PAID_OFF);
        }

        validatePaymentDate(request.getPaymentDate(), debt);

        interestAccrualService.accrueInterest(debt, request.getPaymentDate());

        BigDecimal amount = request.getAmount();
        BigDecimal interest = debt.getAccruedInterest();
        BigDecimal principal = debt.getRemainingPrincipal();

        BigDecimal interestPaid = amount.min(interest);
        BigDecimal remainingAfterInterest = amount.subtract(interestPaid);
        BigDecimal principalPaid = remainingAfterInterest.min(principal);

        debt.setAccruedInterest(interest.subtract(interestPaid));
        debt.setRemainingPrincipal(principal.subtract(principalPaid));
        debt.setLastPaymentDate(request.getPaymentDate());

        debtStateService.moveNextDueDate(debt, request.getAmount());
        debtStateService.refreshDebtStatus(debt);

        if (debt.getStatus() == DebtStatus.PAID_OFF) {
            debt.setPaidOffAt(LocalDateTime.now());
        }
        debtRepository.save(debt);

        Payment payment = paymentMapper.toEntity(request, debt);
        payment.setInterestPaid(interestPaid);
        payment.setPrincipalPaid(principalPaid);

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UserDetails userDetails, Long id) {
        User user = getUserByUsername(userDetails.getUsername());
        Payment payment = paymentRepository
                .findByIdAndDebtUserId(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (payment.isDeleted()) {
            throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentListItem> getPayments(UserDetails userDetails, Long debtId) {
        User user = getUserByUsername(userDetails.getUsername());
        debtRepository.findByIdAndUserIdAndDeletedFalse(debtId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));

        List<Payment> payments = paymentRepository.findByDebtId(debtId);
        List<PaymentListItem> items = new ArrayList<>();
        for (Payment payment : payments) {
            if (!payment.isDeleted()) {
                items.add(paymentMapper.toListItem(payment));
            }
        }
        return items;
    }

    @Transactional(readOnly = true)
    public List<PaymentListItem> getAllPayments(UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        List<Payment> payments = paymentRepository.findAllByUserId(user.getId());
        List<PaymentListItem> items = new ArrayList<>();
        for (Payment payment : payments) {
            items.add(paymentMapper.toListItem(payment));
        }
        return items;
    }

    
    @Transactional
    public PaymentResponse updatePayment(Long id, UpdatePaymentRequest request, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Payment payment = paymentRepository
                .findByIdAndDebtUserId(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (payment.isDeleted()) {
            throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        if (request.getNote() != null) {
            payment.setNote(request.getNote());
        }
        if (request.getPaymentDate() != null) {
            payment.setPaymentDate(request.getPaymentDate());
        }
        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    
    @Transactional
    public void deletePayment(Long id, UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Payment payment = paymentRepository
                .findByIdAndDebtUserId(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        if (payment.isDeleted()) {
            throw new AppException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        payment.setDeleted(true);
        paymentRepository.save(payment);
    }

    private void validatePaymentDate(LocalDate paymentDate, Debt debt) {
        if (paymentDate.isAfter(LocalDate.now())
                || paymentDate.isBefore(debt.getStartDate())
                || (debt.getLastPaymentDate() != null
                && paymentDate.isBefore(debt.getLastPaymentDate()))) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_DATE);
        }
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
