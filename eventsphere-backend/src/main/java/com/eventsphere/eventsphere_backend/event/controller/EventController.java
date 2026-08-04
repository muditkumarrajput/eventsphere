package com.eventsphere.eventsphere_backend.event.controller;

import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // =========================================================
    // CREATE EVENT
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public EventResponse createEvent(
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication) {

        return eventService.createEvent(
                request,
                authentication.getName()
        );
    }

    // =========================================================
    // GET ALL EVENTS
    // =========================================================

    @GetMapping
    public List<EventResponse> getAllEvents() {

        return eventService.getAllEvents();
    }

    // =========================================================
    // SEARCH EVENTS
    // =========================================================

    @GetMapping("/search")
    public List<EventResponse> searchEvents(
            @RequestParam String keyword) {

        return eventService.searchEvents(keyword);
    }

    // =========================================================
    // FILTER BY CATEGORY
    // =========================================================

    @GetMapping("/category/{category}")
    public List<EventResponse> getEventsByCategory(
            @PathVariable EventCategory category) {

        return eventService.getEventsByCategory(category);
    }

    // =========================================================
    // FILTER BY LOCATION
    // =========================================================

    @GetMapping("/location/{location}")
    public List<EventResponse> getEventsByLocation(
            @PathVariable String location) {

        return eventService.getEventsByLocation(location);
    }

    // =========================================================
    // FILTER BY DATE
    // =========================================================

    @GetMapping("/date/{date}")
    public List<EventResponse> getEventsByDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return eventService.getEventsByDate(date);
    }

    // =========================================================
    // FILTER BY PRICE
    // =========================================================

    @GetMapping("/price")
    public List<EventResponse> getEventsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        return eventService.getEventsByPriceRange(
                minPrice,
                maxPrice
        );
    }

    // =========================================================
    // UPCOMING EVENTS
    // =========================================================

    @GetMapping("/upcoming")
    public List<EventResponse> getUpcomingEvents() {

        return eventService.getUpcomingEvents();
    }

    // =========================================================
    // MY EVENTS
    // =========================================================

    @GetMapping("/my-events")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public List<EventResponse> getMyEvents(
            Authentication authentication) {

        return eventService.getMyEvents(
                authentication.getName()
        );
    }

    // =========================================================
    // PAGINATION + SORTING
    // =========================================================

    @GetMapping("/page")
    public Page<EventResponse> getEvents(
            Pageable pageable) {

        return eventService.getEvents(pageable);
    }

    // =========================================================
    // GET EVENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public EventResponse getEventById(
            @PathVariable Long id) {

        return eventService.getEventById(id);
    }

    // =========================================================
    // UPDATE EVENT
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public EventResponse updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request,
            Authentication authentication) {

        return eventService.updateEvent(
                id,
                request,
                authentication.getName()
        );
    }

    // =========================================================
    // DELETE EVENT
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {

        eventService.deleteEvent(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public Page<EventResponse> filterEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            Pageable pageable) {

        return eventService.filterEvents(
                keyword,
                category,
                location,
                minPrice,
                maxPrice,
                startDate,
                endDate,
                pageable
        );
    }
}