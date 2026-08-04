package com.eventsphere.eventsphere_backend.dashboard.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerDashboardResponse {

    private Long totalEvents;

    private Long upcomingEvents;

    private Long completedEvents;

    private Long totalBookings;

    private Long ticketsSold;

    private BigDecimal totalRevenue;
}