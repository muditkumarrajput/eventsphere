package com.eventsphere.eventsphere_backend.notification.controller;

import com.eventsphere.eventsphere_backend.notification.dto.NotificationResponse;
import com.eventsphere.eventsphere_backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "Notifications",
        description = "APIs for managing user notifications"
)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    // =========================================================
    // GET ALL NOTIFICATIONS
    // =========================================================

    @GetMapping
    @Operation(
            summary = "Get my notifications",
            description = "Returns all notifications for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notifications retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            )
    })
    public List<NotificationResponse> getMyNotifications(
            Authentication authentication) {

        return notificationService.getMyNotifications(
                authentication.getName()
        );
    }

    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @GetMapping("/unread")
    @Operation(
            summary = "Get unread notifications",
            description = "Returns all unread notifications for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Unread notifications retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            )
    })
    public List<NotificationResponse> getUnreadNotifications(
            Authentication authentication) {

        return notificationService.getUnreadNotifications(
                authentication.getName()
        );
    }

    // =========================================================
    // GET UNREAD COUNT
    // =========================================================

    @GetMapping("/unread/count")
    @Operation(
            summary = "Get unread notification count",
            description = "Returns the number of unread notifications for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Unread notification count retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            )
    })
    public long getUnreadCount(
            Authentication authentication) {

        return notificationService.getUnreadCount(
                authentication.getName()
        );
    }

    // =========================================================
    // MARK NOTIFICATION AS READ
    // =========================================================

    @PutMapping("/{id}/read")
    @Operation(
            summary = "Mark notification as read",
            description = "Marks a notification as read for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Notification marked as read successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found"
            )
    })
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        notificationService.markAsRead(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }
}