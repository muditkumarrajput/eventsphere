package com.eventsphere.eventsphere_backend.dashboard.controller;

import com.eventsphere.eventsphere_backend.dashboard.dto.EventInsightResponse;
import com.eventsphere.eventsphere_backend.dashboard.dto.OrganizerDashboardResponse;
import com.eventsphere.eventsphere_backend.dashboard.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DashboardController dashboardController;


    // =========================================================
    // GET DASHBOARD
    // =========================================================

    @Test
    void shouldGetDashboardSuccessfully() {

        // Arrange
        String email = "organizer@test.com";

        OrganizerDashboardResponse response =
                OrganizerDashboardResponse.builder()
                        .build();

        when(authentication.getName())
                .thenReturn(email);

        when(dashboardService.getDashboard(email))
                .thenReturn(response);

        // Act
        OrganizerDashboardResponse result =
                dashboardController.getDashboard(authentication);

        // Assert
        assertNotNull(result);
        assertSame(response, result);

        verify(authentication)
                .getName();

        verify(dashboardService)
                .getDashboard(email);
    }


    @Test
    void shouldPassAuthenticatedUserEmailToDashboardService() {

        // Arrange
        String email = "admin@test.com";

        OrganizerDashboardResponse response =
                OrganizerDashboardResponse.builder()
                        .build();

        when(authentication.getName())
                .thenReturn(email);

        when(dashboardService.getDashboard(email))
                .thenReturn(response);

        // Act
        dashboardController.getDashboard(authentication);

        // Assert
        verify(dashboardService)
                .getDashboard(email);
    }


    @Test
    void shouldReturnExactDashboardResponseFromService() {

        // Arrange
        String email = "organizer@test.com";

        OrganizerDashboardResponse response =
                OrganizerDashboardResponse.builder()
                        .build();

        when(authentication.getName())
                .thenReturn(email);

        when(dashboardService.getDashboard(email))
                .thenReturn(response);

        // Act
        OrganizerDashboardResponse result =
                dashboardController.getDashboard(authentication);

        // Assert
        assertSame(response, result);
    }


    // =========================================================
    // GET EVENT INSIGHTS
    // =========================================================

    @Test
    void shouldGetEventInsightsSuccessfully() {

        // Arrange
        String email = "organizer@test.com";

        EventInsightResponse insight1 =
                EventInsightResponse.builder()
                        .build();

        EventInsightResponse insight2 =
                EventInsightResponse.builder()
                        .build();

        List<EventInsightResponse> response =
                List.of(insight1, insight2);

        when(authentication.getName())
                .thenReturn(email);

        when(dashboardService.getEventInsights(email))
                .thenReturn(response);

        // Act
        List<EventInsightResponse> result =
                dashboardController.getEventInsights(authentication);

        // Assert
        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        assertSame(
                insight1,
                result.get(0)
        );

        assertSame(
                insight2,
                result.get(1)
        );

        verify(authentication)
                .getName();

        verify(dashboardService)
                .getEventInsights(email);
    }


    @Test
    void shouldPassAuthenticatedUserEmailToEventInsightsService() {

        // Arrange
        String email = "admin@test.com";

        when(authentication.getName())
                .thenReturn(email);

        when(dashboardService.getEventInsights(email))
                .thenReturn(List.of());

        // Act
        dashboardController.getEventInsights(authentication);

        // Assert
        verify(dashboardService)
                .getEventInsights(email);
    }


    @Test
    void shouldReturnEmptyEventInsightsWhenServiceReturnsEmptyList() {

        // Arrange
        String email = "organizer@test.com";

        when(authentication.getName())
                .thenReturn(email);

        when(dashboardService.getEventInsights(email))
                .thenReturn(List.of());

        // Act
        List<EventInsightResponse> result =
                dashboardController.getEventInsights(authentication);

        // Assert
        assertNotNull(result);

        assertTrue(result.isEmpty());

        verify(dashboardService)
                .getEventInsights(email);
    }
}