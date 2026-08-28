package com.eventsphere.eventsphere_backend.dashboard.controller;

import com.eventsphere.eventsphere_backend.dashboard.dto.EventInsightResponse;
import com.eventsphere.eventsphere_backend.dashboard.dto.OrganizerDashboardResponse;
import com.eventsphere.eventsphere_backend.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(
        name = "Dashboard",
        description = "Organizer and administrator dashboard analytics APIs"
)
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // =========================================================
    // DASHBOARD SUMMARY
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @Operation(
            summary = "Get dashboard summary",
            description = "Returns dashboard statistics for the authenticated organizer or administrator"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard data retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to access the dashboard"
            )
    })
    public OrganizerDashboardResponse getDashboard(
            Authentication authentication) {

        return dashboardService.getDashboard(
                authentication.getName()
        );
    }

    // =========================================================
    // EVENT INSIGHTS
    // =========================================================

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @Operation(
            summary = "Get event insights",
            description = "Returns event-wise analytics including booking and revenue insights"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event insights retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to access event insights"
            )
    })
    public List<EventInsightResponse> getEventInsights(
            Authentication authentication) {

        return dashboardService.getEventInsights(
                authentication.getName()
        );
    }
}