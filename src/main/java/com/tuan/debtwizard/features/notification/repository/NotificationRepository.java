package com.tuan.debtwizard.features.notification.repository;

import com.tuan.debtwizard.features.notification.dto.NotificationResponse;
import com.tuan.debtwizard.features.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    Optional<Notification> findByIdAndUserIdAndDeletedFalse(Long notificationId, Long userId);

    List<Notification> findByUserIdAndDeletedFalse(Long id);
}
