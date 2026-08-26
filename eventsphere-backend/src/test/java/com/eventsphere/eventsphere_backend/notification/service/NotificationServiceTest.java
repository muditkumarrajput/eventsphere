package com.eventsphere.eventsphere_backend.notification.service;

import com.eventsphere.eventsphere_backend.common.exception.NotificationNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.notification.dto.NotificationResponse;
import com.eventsphere.eventsphere_backend.notification.entity.Notification;
import com.eventsphere.eventsphere_backend.notification.repository.NotificationRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private UserRepository userRepository;

    private NotificationService notificationService;


    @BeforeEach
    void setUp() {

        notificationRepository =
                mock(NotificationRepository.class);

        userRepository =
                mock(UserRepository.class);

        notificationService =
                new NotificationService(
                        notificationRepository,
                        userRepository
                );
    }


    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    @Test
    void shouldCreateNotificationSuccessfully() {

        User user = mock(User.class);
        Notification notification = mock(Notification.class);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(notificationRepository.save(
                any(Notification.class)
        )).thenReturn(notification);

        when(notification.getId())
                .thenReturn(5L);

        when(notification.getTitle())
                .thenReturn("Event Reminder");

        when(notification.getMessage())
                .thenReturn("Your event starts tomorrow.");

        when(notification.isRead())
                .thenReturn(false);

        NotificationResponse result =
                notificationService.createNotification(
                        10L,
                        "Event Reminder",
                        "Your event starts tomorrow."
                );

        assertEquals(5L, result.id());

        assertEquals(
                "Event Reminder",
                result.title()
        );

        assertEquals(
                "Your event starts tomorrow.",
                result.message()
        );

        assertFalse(result.isRead());

        verify(userRepository)
                .findById(10L);

        verify(notificationRepository)
                .save(any(Notification.class));
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileCreatingNotification() {

        when(userRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> notificationService.createNotification(
                        10L,
                        "Event Reminder",
                        "Your event starts tomorrow."
                )
        );

        verify(userRepository)
                .findById(10L);

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }


    // =========================================================
    // GET MY NOTIFICATIONS
    // =========================================================

    @Test
    void shouldGetMyNotificationsSuccessfully() {

        User user = mock(User.class);
        Notification notification = mock(Notification.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(notification));

        when(notification.getId())
                .thenReturn(5L);

        when(notification.getTitle())
                .thenReturn("Event Reminder");

        when(notification.getMessage())
                .thenReturn("Your event starts tomorrow.");

        when(notification.isRead())
                .thenReturn(false);

        List<NotificationResponse> result =
                notificationService.getMyNotifications(
                        "user@test.com"
                );

        assertEquals(1, result.size());

        assertEquals(
                5L,
                result.get(0).id()
        );

        assertEquals(
                "Event Reminder",
                result.get(0).title()
        );

        assertEquals(
                "Your event starts tomorrow.",
                result.get(0).message()
        );

        assertFalse(
                result.get(0).isRead()
        );

        verify(userRepository)
                .findByEmail("user@test.com");

        verify(notificationRepository)
                .findByUserOrderByCreatedAtDesc(user);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileGettingMyNotifications() {

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> notificationService.getMyNotifications(
                        "unknown@test.com"
                )
        );

        verify(notificationRepository, never())
                .findByUserOrderByCreatedAtDesc(
                        any(User.class)
                );
    }


    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @Test
    void shouldGetUnreadNotificationsSuccessfully() {

        User user = mock(User.class);
        Notification notification = mock(Notification.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user))
                .thenReturn(List.of(notification));

        when(notification.getId())
                .thenReturn(5L);

        when(notification.getTitle())
                .thenReturn("Payment Successful");

        when(notification.getMessage())
                .thenReturn("Your payment was successful.");

        when(notification.isRead())
                .thenReturn(false);

        List<NotificationResponse> result =
                notificationService.getUnreadNotifications(
                        "user@test.com"
                );

        assertEquals(1, result.size());

        assertEquals(
                5L,
                result.get(0).id()
        );

        assertEquals(
                "Payment Successful",
                result.get(0).title()
        );

        assertEquals(
                "Your payment was successful.",
                result.get(0).message()
        );

        assertFalse(
                result.get(0).isRead()
        );

        verify(userRepository)
                .findByEmail("user@test.com");

        verify(notificationRepository)
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(
                        user
                );
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileGettingUnreadNotifications() {

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> notificationService.getUnreadNotifications(
                        "unknown@test.com"
                )
        );

        verify(notificationRepository, never())
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(
                        any(User.class)
                );
    }


    // =========================================================
    // GET UNREAD COUNT
    // =========================================================

    @Test
    void shouldGetUnreadCountSuccessfully() {

        User user = mock(User.class);

        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .countByUserAndIsReadFalse(user))
                .thenReturn(3L);

        long result =
                notificationService.getUnreadCount(
                        "user@test.com"
                );

        assertEquals(3L, result);

        verify(userRepository)
                .findByEmail("user@test.com");

        verify(notificationRepository)
                .countByUserAndIsReadFalse(user);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileGettingUnreadCount() {

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> notificationService.getUnreadCount(
                        "unknown@test.com"
                )
        );

        verify(notificationRepository, never())
                .countByUserAndIsReadFalse(
                        any(User.class)
                );
    }


    // =========================================================
    // MARK AS READ
    // =========================================================

    @Test
    void shouldMarkNotificationAsReadSuccessfully() {

        Notification notification =
                mock(Notification.class);

        User user =
                mock(User.class);

        when(notificationRepository.findById(5L))
                .thenReturn(Optional.of(notification));

        when(notification.getUser())
                .thenReturn(user);

        when(user.getEmail())
                .thenReturn("user@test.com");

        notificationService.markAsRead(
                5L,
                "user@test.com"
        );

        verify(notification)
                .setRead(true);

        verify(notificationRepository)
                .save(notification);
    }


    @Test
    void shouldThrowExceptionWhenNotificationNotFoundWhileMarkingAsRead() {

        when(notificationRepository.findById(5L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotificationNotFoundException.class,
                () -> notificationService.markAsRead(
                        5L,
                        "user@test.com"
                )
        );

        verify(notificationRepository)
                .findById(5L);

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotOwnNotification() {

        Notification notification =
                mock(Notification.class);

        User user =
                mock(User.class);

        when(notificationRepository.findById(5L))
                .thenReturn(Optional.of(notification));

        when(notification.getUser())
                .thenReturn(user);

        when(user.getEmail())
                .thenReturn("other@test.com");

        assertThrows(
                NotificationNotFoundException.class,
                () -> notificationService.markAsRead(
                        5L,
                        "user@test.com"
                )
        );

        verify(notification, never())
                .setRead(true);

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }
}
