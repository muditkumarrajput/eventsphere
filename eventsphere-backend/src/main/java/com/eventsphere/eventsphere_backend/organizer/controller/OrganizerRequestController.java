package com.eventsphere.eventsphere_backend.organizer.controller;

import com.eventsphere.eventsphere_backend.organizer.dto.CreateOrganizerRequest;
import com.eventsphere.eventsphere_backend.organizer.dto.OrganizerRequestResponse;
import com.eventsphere.eventsphere_backend.organizer.service.OrganizerRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizer-requests")
public class OrganizerRequestController {

    private final OrganizerRequestService organizerRequestService;

    public OrganizerRequestController(
            OrganizerRequestService organizerRequestService) {

        this.organizerRequestService =
                organizerRequestService;
    }

    // =========================================================
    // CREATE ORGANIZER REQUEST
    // USER ONLY
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public OrganizerRequestResponse createRequest(
            @Valid @RequestBody CreateOrganizerRequest request,
            Authentication authentication) {

        return organizerRequestService.createRequest(
                authentication.getName(),
                request
        );
    }

    // =========================================================
    // GET CURRENT USER REQUESTS
    // USER ONLY
    // =========================================================

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<OrganizerRequestResponse> getMyRequests(
            Authentication authentication) {

        return organizerRequestService.getMyRequests(
                authentication.getName()
        );
    }

    // =========================================================
    // GET ALL ORGANIZER REQUESTS
    // ADMIN ONLY
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrganizerRequestResponse> getAllRequests() {

        return organizerRequestService.getAllRequests();
    }

    // =========================================================
    // APPROVE ORGANIZER REQUEST
    // ADMIN ONLY
    // =========================================================

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizerRequestResponse approveRequest(
            @PathVariable("id") Long id,
            Authentication authentication) {

        return organizerRequestService.approveRequest(
                id,
                authentication.getName()
        );
    }

    // =========================================================
    // REJECT ORGANIZER REQUEST
    // ADMIN ONLY
    // =========================================================

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizerRequestResponse rejectRequest(
            @PathVariable("id") Long id,
            Authentication authentication) {

        return organizerRequestService.rejectRequest(
                id,
                authentication.getName()
        );
    }
}