package com.tuan.debtwizard.features.notification.model;

import com.tuan.debtwizard.features.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;
    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @Column(nullable = false)
    private boolean deleted = false;

    private boolean isRead = false;
    @Column(nullable =false)
    private LocalDateTime createdAt;
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
