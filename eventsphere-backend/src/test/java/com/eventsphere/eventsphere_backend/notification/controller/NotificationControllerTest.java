package com.eventsphere.eventsphere_backend.notification.controller;

import com.eventsphere.eventsphere_backend.notification.dto.NotificationResponse;
import com.eventsphere.eventsphere_backend.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private MockMvc mockMvc;

    private NotificationService notificationService;

    private Authentication authentication;


    @BeforeEach
    void setUp() {

        notificationService = mock(NotificationService.class);

        authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("user@test.com");

        NotificationController notificationController =
                new NotificationController(notificationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(notificationController)
                .build();
    }


    // =========================================================
    // GET MY NOTIFICATIONS
    // =========================================================

    @Test
    void shouldGetMyNotifications() throws Exception {

        NotificationResponse response =
                NotificationResponse.builder()
                        .id(1L)
                        .title("Booking Confirmed")
                        .message("Your booking has been confirmed.")
                        .isRead(false)
                        .build();

        when(notificationService.getMyNotifications("user@test.com"))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/notifications")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Booking Confirmed")
                )
                .andExpect(
                        jsonPath("$[0].message")
                                .value("Your booking has been confirmed.")
                )
                .andExpect(
                        jsonPath("$[0].isRead")
                                .value(false)
                );

        verify(notificationService)
                .getMyNotifications("user@test.com");
    }


    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @Test
    void shouldGetUnreadNotifications() throws Exception {

        NotificationResponse response =
                NotificationResponse.builder()
                        .id(2L)
                        .title("Event Reminder")
                        .message("Your event is starting soon.")
                        .isRead(false)
                        .build();

        when(notificationService.getUnreadNotifications("user@test.com"))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/notifications/unread")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Event Reminder")
                )
                .andExpect(
                        jsonPath("$[0].message")
                                .value("Your event is starting soon.")
                )
                .andExpect(
                        jsonPath("$[0].isRead")
                                .value(false)
                );

        verify(notificationService)
                .getUnreadNotifications("user@test.com");
    }


    // =========================================================
    // GET UNREAD COUNT
    // =========================================================

    @Test
    void shouldGetUnreadCount() throws Exception {

        when(notificationService.getUnreadCount("user@test.com"))
                .thenReturn(3L);

        mockMvc.perform(
                        get("/api/notifications/unread/count")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));

        verify(notificationService)
                .getUnreadCount("user@test.com");
    }


    // =========================================================
    // MARK AS READ
    // =========================================================

    @Test
    void shouldMarkNotificationAsRead() throws Exception {

        mockMvc.perform(
                        put("/api/notifications/5/read")
                                .principal(authentication)
                )
                .andExpect(status().isNoContent());

        verify(notificationService)
                .markAsRead(
                        5L,
                        "user@test.com"
                );
    }
}