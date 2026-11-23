package com.hlala.service;

import com.hlala.domain.AdminProfile;
import com.hlala.domain.Enums;
import com.hlala.domain.Notification;
import com.hlala.exception.ForbiddenOperationException;
import com.hlala.exception.NotFoundException;
import com.hlala.factory.CareFactory;
import com.hlala.repository.AdminProfileRepository;
import com.hlala.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Stores in-app notifications. SMS fallback (for the "alerts by SMS" toggle on
 * the Saved screen) would be a second channel added behind this same interface.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifications;
    private final AdminProfileRepository admins;
    private final CareFactory factory;

    public NotificationServiceImpl(NotificationRepository notifications, AdminProfileRepository admins,
                                   CareFactory factory) {
        this.notifications = notifications;
        this.admins = admins;
        this.factory = factory;
    }

    @Override
    @Transactional
    public void notify(UUID userId, Enums.NotificationType type, String payloadRef) {
        notifications.save(factory.notification(userId, type, payloadRef));
    }

    @Override
    @Transactional
    public void notifyAdmins(Enums.NotificationType type, String payloadRef) {
        for (AdminProfile admin : admins.findAll()) {
            notify(admin.getUserId(), type, payloadRef);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> forUser(Actor actor) {
        return notifications.findByUserIdOrderByCreatedAtDesc(actor.userId());
    }

    @Override
    @Transactional
    public Notification markRead(Actor actor, UUID notificationId) {
        Notification n = notifications.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        if (!n.getUserId().equals(actor.userId())) {
            throw new ForbiddenOperationException("This notification is not yours");
        }
        return notifications.save(n.markRead());
    }

    @Override
    @Transactional
    public void markAllRead(Actor actor) {
        for (Notification n : notifications.findByUserIdOrderByCreatedAtDesc(actor.userId())) {
            if (!n.isRead()) {
                notifications.save(n.markRead());
            }
        }
    }
}
