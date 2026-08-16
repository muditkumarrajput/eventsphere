package com.eventsphere.eventsphere_backend.notification.service;

import com.eventsphere.eventsphere_backend.common.exception.NotificationNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.notification.dto.NotificationResponse;
import com.eventsphere.eventsphere_backend.notification.entity.Notification;
import com.eventsphere.eventsphere_backend.notification.repository.NotificationRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // ===========================
    // Create Notification
    // ===========================

    public NotificationResponse createNotification(
            Long userId,
            String title,
            String message) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(userId));

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .isRead(false)
                .user(user)
                .build();

        return toResponse(
                notificationRepository.save(notification)
        );
    }

    // ===========================
    // Get My Notifications
    // ===========================

    public List<NotificationResponse> getMyNotifications(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ===========================
    // Get Unread Notifications
    // ===========================

    public List<NotificationResponse> getUnreadNotifications(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ===========================
    // Get Unread Notification Count
    // ===========================

    public long getUnreadCount(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return notificationRepository
                .countByUserAndIsReadFalse(user);
    }

    // ===========================
    // Mark Notification as Read
    // ===========================

    public void markAsRead(
            Long notificationId,
            String email) {

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new NotificationNotFoundException(
                                        notificationId
                                ));

        // Check notification ownership
        if (!notification.getUser().getEmail().equals(email)) {

            // Deliberately return "not found" rather than
            // revealing that another user's notification exists.
            throw new NotificationNotFoundException(
                    notificationId
            );
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    // ===========================
    // Convert Entity to Response
    // ===========================

    private NotificationResponse toResponse(
            Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}