package com.tuan.debtwizard.features.notification.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.event.PaymentCompletedEvent;
import com.tuan.debtwizard.features.event.PaymentReminderEvent;
import com.tuan.debtwizard.features.notification.dto.NotificationResponse;
import com.tuan.debtwizard.features.notification.mapper.NotificationMapper;
import com.tuan.debtwizard.features.notification.model.Notification;
import com.tuan.debtwizard.features.notification.model.NotificationType;
import com.tuan.debtwizard.features.notification.repository.NotificationRepository;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.userRepository = userRepository;
    }


    @Transactional(readOnly = true)
    public List<NotificationResponse>  getNotifications(UserDetails userDetails){
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND)) ;
        List<Notification> notifications = notificationRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(user.getId());
        List<NotificationResponse> responses= new ArrayList<>();
        for(Notification notification : notifications){
            NotificationResponse response = notificationMapper.toResponse(notification);
            responses.add(response);
        }
        return responses;
    }
    @Transactional
    public void markAsRead(Long notificationId, UserDetails userDetails){
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Notification notification = notificationRepository
                .findByIdAndUserIdAndDeletedFalse(notificationId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        notification.setRead(true);
    }
    @Transactional
    public void markAllAsRead(UserDetails userDetails){
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Notification> notifications = notificationRepository.findByUserIdAndDeletedFalse(user.getId());
        for(Notification notification : notifications){
            notification.setRead(true);
        }
    }
    @Transactional
    public void createPaymentReminder(PaymentReminderEvent event){
        String referenceKey = "PAYMENT_REMINDER_" + event.debtId() + "_" + event.nextDueDate();
        if(notificationRepository.existsByReferenceKey(referenceKey)){
            return;
        }
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Payment Reminder");
        notification.setMessage(
                "Khoản nợ " + event.lenderName()
                        + " sẽ đến hạn vào ngày " + event.nextDueDate() + ". Số tiền cần trả: "
                        + event.expectedMonthlyPayment() + " VNĐ");
        notification.setType(NotificationType.PAYMENT_REMINDER);
        notification.setRead(false);
        notification.setReferenceKey(referenceKey);
        notificationRepository.save(notification);
    }
    @Transactional
    public void createPaymentCompletedNotification(PaymentCompletedEvent event) {
        String referenceKey = "PAYMENT_COMPLETED_" + event.paymentId();
        if(notificationRepository.existsByReferenceKey(referenceKey)) {
            return;
        }
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Payment Completed");
        notification.setMessage("Bạn đã thanh toán " + event.amount() + " cho khoản nợ "+event.lenderName());
        notification.setType(NotificationType.PAYMENT_COMPLETED);
        notification.setRead(false);
        notification.setReferenceKey(referenceKey);
        notificationRepository.save(notification);
    }
    @Transactional
    public void createNotification(User user, String title, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(notificationType);
        notification.setRead(false);
        notificationRepository.save(notification);
    }
}
