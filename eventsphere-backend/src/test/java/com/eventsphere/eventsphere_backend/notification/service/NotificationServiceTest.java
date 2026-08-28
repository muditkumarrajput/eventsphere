package com.eventsphere.eventsphere_backend.notification.service;

import com.eventsphere.eventsphere_backend.common.exception.NotificationNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.notification.dto.NotificationResponse;
import com.eventsphere.eventsphere_backend.notification.entity.Notification;
import com.eventsphere.eventsphere_backend.notification.repository.NotificationRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;


    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    @Test
    void shouldCreateNotificationSuccessfully() {

        User user = new User();
        user.setId(10L);
        user.setEmail("user@test.com");

        Notification notification = Notification.builder()
                .id(5L)
                .title("Event Reminder")
                .message("Your event starts tomorrow.")
                .isRead(false)
                .user(user)
                .build();

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        NotificationResponse result =
                notificationService.createNotification(
                        10L,
                        "Event Reminder",
                        "Your event starts tomorrow."
                );

        assertEquals(5L, result.id());
        assertEquals("Event Reminder", result.title());
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

        String email = "user@test.com";

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        Notification notification =
                Notification.builder()
                        .id(5L)
                        .title("Event Reminder")
                        .message("Your event starts tomorrow.")
                        .isRead(false)
                        .user(user)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(notification));

        List<NotificationResponse> result =
                notificationService.getMyNotifications(email);

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
                .findByEmail(email);

        verify(notificationRepository)
                .findByUserOrderByCreatedAtDesc(user);
    }


    @Test
    void shouldReturnEmptyListWhenUserHasNoNotifications() {

        String email = "user@test.com";

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of());

        List<NotificationResponse> result =
                notificationService.getMyNotifications(email);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository)
                .findByEmail(email);

        verify(notificationRepository)
                .findByUserOrderByCreatedAtDesc(user);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileGettingMyNotifications() {

        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> notificationService.getMyNotifications(email)
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(notificationRepository);
    }


    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @Test
    void shouldGetUnreadNotificationsSuccessfully() {

        String email = "user@test.com";

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        Notification notification =
                Notification.builder()
                        .id(5L)
                        .title("Payment Successful")
                        .message("Your payment was successful.")
                        .isRead(false)
                        .user(user)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user))
                .thenReturn(List.of(notification));

        List<NotificationResponse> result =
                notificationService.getUnreadNotifications(email);

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
                .findByEmail(email);

        verify(notificationRepository)
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }


    @Test
    void shouldReturnEmptyListWhenThereAreNoUnreadNotifications() {

        String email = "user@test.com";

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user))
                .thenReturn(List.of());

        List<NotificationResponse> result =
                notificationService.getUnreadNotifications(email);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository)
                .findByEmail(email);

        verify(notificationRepository)
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileGettingUnreadNotifications() {

        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> notificationService.getUnreadNotifications(email)
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(notificationRepository);
    }


    // =========================================================
    // GET UNREAD COUNT
    // =========================================================

    @Test
    void shouldGetUnreadCountSuccessfully() {

        String email = "user@test.com";

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .countByUserAndIsReadFalse(user))
                .thenReturn(3L);

        long result =
                notificationService.getUnreadCount(email);

        assertEquals(3L, result);

        verify(userRepository)
                .findByEmail(email);

        verify(notificationRepository)
                .countByUserAndIsReadFalse(user);
    }


    @Test
    void shouldReturnZeroWhenThereAreNoUnreadNotifications() {

        String email = "user@test.com";

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .countByUserAndIsReadFalse(user))
                .thenReturn(0L);

        long result =
                notificationService.getUnreadCount(email);

        assertEquals(0L, result);

        verify(userRepository)
                .findByEmail(email);

        verify(notificationRepository)
                .countByUserAndIsReadFalse(user);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFoundWhileGettingUnreadCount() {

        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> notificationService.getUnreadCount(email)
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(notificationRepository);
    }


    // =========================================================
    // MARK AS READ
    // =========================================================

    @Test
    void shouldMarkNotificationAsReadSuccessfully() {

        Long notificationId = 5L;
        String email = "user@test.com";

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        Notification notification =
                Notification.builder()
                        .id(notificationId)
                        .title("Event Reminder")
                        .message("Your event starts tomorrow.")
                        .isRead(false)
                        .user(user)
                        .build();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(
                notificationId,
                email
        );

        assertTrue(notification.isRead());

        verify(notificationRepository)
                .findById(notificationId);

        verify(notificationRepository)
                .save(notification);
    }


    @Test
    void shouldThrowExceptionWhenNotificationNotFoundWhileMarkingAsRead() {

        Long notificationId = 5L;
        String email = "user@test.com";

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotificationNotFoundException.class,
                () -> notificationService.markAsRead(
                        notificationId,
                        email
                )
        );

        verify(notificationRepository)
                .findById(notificationId);

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotOwnNotification() {

        Long notificationId = 5L;

        String ownerEmail = "owner@test.com";
        String otherEmail = "other@test.com";

        User owner = new User();
        owner.setId(10L);
        owner.setEmail(ownerEmail);

        Notification notification =
                Notification.builder()
                        .id(notificationId)
                        .title("Private Notification")
                        .message("Private message")
                        .isRead(false)
                        .user(owner)
                        .build();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        assertThrows(
                NotificationNotFoundException.class,
                () -> notificationService.markAsRead(
                        notificationId,
                        otherEmail
                )
        );

        assertFalse(notification.isRead());

        verify(notificationRepository)
                .findById(notificationId);

        verify(notificationRepository, never())
                .save(any(Notification.class));
    }


    @Test
    void shouldAllowOwnerToMarkAlreadyReadNotificationAsRead() {

        Long notificationId = 5L;
        String email = "user@test.com";

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        Notification notification =
                Notification.builder()
                        .id(notificationId)
                        .title("Event Reminder")
                        .message("Your event starts tomorrow.")
                        .isRead(true)
                        .user(user)
                        .build();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(
                notificationId,
                email
        );

        assertTrue(notification.isRead());

        verify(notificationRepository)
                .findById(notificationId);

        verify(notificationRepository)
                .save(notification);
    }
}