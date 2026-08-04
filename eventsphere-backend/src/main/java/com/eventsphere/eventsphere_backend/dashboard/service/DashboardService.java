package com.eventsphere.eventsphere_backend.dashboard.service;

import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.dashboard.dto.EventInsightResponse;
import com.eventsphere.eventsphere_backend.dashboard.dto.OrganizerDashboardResponse;
import com.eventsphere.eventsphere_backend.dashboard.repository.DashboardRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final UserRepository userRepository;

    public DashboardService(
            DashboardRepository dashboardRepository,
            UserRepository userRepository) {

        this.dashboardRepository = dashboardRepository;
        this.userRepository = userRepository;
    }

    public OrganizerDashboardResponse getDashboard(String email) {

        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        long totalEvents =
                dashboardRepository.countByCreatedBy(organizer);

        long upcomingEvents =
                dashboardRepository.countUpcomingEvents(organizer);

        long completedEvents =
                dashboardRepository.countCompletedEvents(organizer);

        long totalBookings =
                dashboardRepository.countTotalBookings(organizer);

        Integer ticketsSold =
                dashboardRepository.sumTicketsSold(organizer);

        BigDecimal totalRevenue =
                dashboardRepository.sumRevenue(organizer);

        return OrganizerDashboardResponse.builder()
                .totalEvents(totalEvents)
                .upcomingEvents(upcomingEvents)
                .completedEvents(completedEvents)
                .totalBookings(totalBookings)
                .ticketsSold(ticketsSold.longValue())
                .totalRevenue(totalRevenue)
                .build();
    }

    public List<EventInsightResponse> getEventInsights(String email) {

        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        List<Object[]> results =
                dashboardRepository.getEventInsights(organizer);

        return results.stream()
                .map(row -> EventInsightResponse.builder()
                        .eventId(((Number) row[0]).longValue())
                        .title((String) row[1])
                        .capacity(((Number) row[2]).intValue())
                        .ticketsSold(((Number) row[3]).longValue())
                        .remainingSeats(((Number) row[4]).intValue())
                        .occupancyPercentage(((Number) row[5]).doubleValue())
                        .revenue((BigDecimal) row[6])
                        .build())
                .toList();
    }
}