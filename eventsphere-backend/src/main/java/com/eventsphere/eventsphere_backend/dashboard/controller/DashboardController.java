package com.eventsphere.eventsphere_backend.dashboard.controller;

import com.eventsphere.eventsphere_backend.dashboard.dto.EventInsightResponse;
import com.eventsphere.eventsphere_backend.dashboard.dto.OrganizerDashboardResponse;
import com.eventsphere.eventsphere_backend.dashboard.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public OrganizerDashboardResponse getDashboard(
            Authentication authentication) {

        return dashboardService.getDashboard(
                authentication.getName()
        );
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public List<EventInsightResponse> getEventInsights(
            Authentication authentication) {

        return dashboardService.getEventInsights(
                authentication.getName()
        );
    }
}