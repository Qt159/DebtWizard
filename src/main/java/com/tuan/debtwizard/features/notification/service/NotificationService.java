package com.tuan.debtwizard.features.notification.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
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
}
