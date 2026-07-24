package com.tuan.debtwizard.features.notification.mapper;

import com.tuan.debtwizard.features.notification.dto.NotificationResponse;
import com.tuan.debtwizard.features.notification.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(Notification notification){
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
