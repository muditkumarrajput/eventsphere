package com.eventsphere.eventsphere_backend.dashboard.service;

import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.dashboard.dto.EventInsightResponse;
import com.eventsphere.eventsphere_backend.dashboard.dto.OrganizerDashboardResponse;
import com.eventsphere.eventsphere_backend.dashboard.repository.DashboardRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;


    // =========================================================
    // GET DASHBOARD
    // =========================================================

    @Test
    void shouldGetOrganizerDashboard() {

        // Arrange
        String email = "organizer@test.com";

        User organizer = new User();
        organizer.setId(3L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(organizer));

        when(dashboardRepository.countByCreatedBy(organizer))
                .thenReturn(5L);

        when(dashboardRepository.countUpcomingEvents(organizer))
                .thenReturn(3L);

        when(dashboardRepository.countCompletedEvents(organizer))
                .thenReturn(2L);

        when(dashboardRepository.countTotalBookings(organizer))
                .thenReturn(10L);

        when(dashboardRepository.sumTicketsSold(organizer))
                .thenReturn(20);

        when(dashboardRepository.sumRevenue(organizer))
                .thenReturn(new BigDecimal("19980.00"));

        // Act
        OrganizerDashboardResponse result =
                dashboardService.getDashboard(email);

        // Assert
        assertEquals(5L, result.getTotalEvents());
        assertEquals(3L, result.getUpcomingEvents());
        assertEquals(2L, result.getCompletedEvents());
        assertEquals(10L, result.getTotalBookings());
        assertEquals(20L, result.getTicketsSold());

        assertEquals(
                new BigDecimal("19980.00"),
                result.getTotalRevenue()
        );

        // Verify repository calls
        verify(dashboardRepository)
                .countByCreatedBy(organizer);

        verify(dashboardRepository)
                .countUpcomingEvents(organizer);

        verify(dashboardRepository)
                .countCompletedEvents(organizer);

        verify(dashboardRepository)
                .countTotalBookings(organizer);

        verify(dashboardRepository)
                .sumTicketsSold(organizer);

        verify(dashboardRepository)
                .sumRevenue(organizer);
    }


    // =========================================================
    // GET EVENT INSIGHTS
    // =========================================================

    @Test
    void shouldGetEventInsights() {

        // Arrange
        String email = "organizer@test.com";

        User organizer = new User();
        organizer.setId(3L);

        Object[] row = new Object[]{
                8L,
                "Java Backend Workshop",
                100,
                20L,
                80,
                20.0,
                new BigDecimal("29980.00")
        };

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(organizer));

        when(dashboardRepository.getEventInsights(organizer))
                .thenReturn(List.<Object[]>of(row));

        // Act
        List<EventInsightResponse> result =
                dashboardService.getEventInsights(email);

        // Assert
        assertEquals(1, result.size());

        EventInsightResponse insight = result.get(0);

        assertEquals(8L, insight.getEventId());

        assertEquals(
                "Java Backend Workshop",
                insight.getTitle()
        );

        assertEquals(
                100,
                insight.getCapacity()
        );

        assertEquals(
                20L,
                insight.getTicketsSold()
        );

        assertEquals(
                80,
                insight.getRemainingSeats()
        );

        assertEquals(
                20.0,
                insight.getOccupancyPercentage()
        );

        assertEquals(
                new BigDecimal("29980.00"),
                insight.getRevenue()
        );

        verify(dashboardRepository)
                .getEventInsights(organizer);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExistForDashboard() {

        // Arrange
        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> dashboardService.getDashboard(email)
        );

        assertEquals(
                "User with email unknown@test.com not found.",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(dashboardRepository);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistForEventInsights() {

        // Arrange
        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> dashboardService.getEventInsights(email)
        );

        assertEquals(
                "User with email unknown@test.com not found.",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(dashboardRepository);
    }
}