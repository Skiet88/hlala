package com.hlala.service;

import com.hlala.domain.Enums;
import com.hlala.domain.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void notify(UUID userId, Enums.NotificationType type, String payloadRef);

    void notifyAdmins(Enums.NotificationType type, String payloadRef);

    List<Notification> forUser(Actor actor);

    Notification markRead(Actor actor, UUID notificationId);

    void markAllRead(Actor actor);
}
