package com.eventsphere.eventsphere_backend.event.controller;

import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Events",
        description = "APIs for creating, searching, filtering, updating, and managing events"
)
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
    @Operation(
            summary = "Create an event",
            description = "Creates a new event for an administrator or organizer"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid event data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to create events"
            )
    })
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
    @Operation(
            summary = "Get all events",
            description = "Returns all available events"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully"
    )
    public List<EventResponse> getAllEvents() {

        return eventService.getAllEvents();
    }

    // =========================================================
    // SEARCH EVENTS
    // =========================================================

    @GetMapping("/search")
    @Operation(
            summary = "Search events",
            description = "Searches events using a keyword"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Keyword is missing or invalid"
            )
    })
    public List<EventResponse> searchEvents(
            @RequestParam String keyword) {

        return eventService.searchEvents(keyword);
    }

    // =========================================================
    // FILTER BY CATEGORY
    // =========================================================

    @GetMapping("/category/{category}")
    @Operation(
            summary = "Get events by category",
            description = "Returns events belonging to the specified category"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Events retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid event category"
            )
    })
    public List<EventResponse> getEventsByCategory(
            @PathVariable EventCategory category) {

        return eventService.getEventsByCategory(category);
    }

    // =========================================================
    // FILTER BY LOCATION
    // =========================================================

    @GetMapping("/location/{location}")
    @Operation(
            summary = "Get events by location",
            description = "Returns events available at the specified location"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully"
    )
    public List<EventResponse> getEventsByLocation(
            @PathVariable String location) {

        return eventService.getEventsByLocation(location);
    }

    // =========================================================
    // FILTER BY DATE
    // =========================================================

    @GetMapping("/date/{date}")
    @Operation(
            summary = "Get events by date",
            description = "Returns events scheduled for the specified date"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Events retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date format"
            )
    })
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
    @Operation(
            summary = "Get events by price range",
            description = "Returns events whose ticket price falls within the specified range"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully"
    )
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
    @Operation(
            summary = "Get upcoming events",
            description = "Returns events scheduled for the future"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Upcoming events retrieved successfully"
    )
    public List<EventResponse> getUpcomingEvents() {

        return eventService.getUpcomingEvents();
    }

    // =========================================================
    // MY EVENTS
    // =========================================================

    @GetMapping("/my-events")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @Operation(
            summary = "Get my events",
            description = "Returns events created by the authenticated administrator or organizer"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Events retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to access this resource"
            )
    })
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
    @Operation(
            summary = "Get paginated events",
            description = "Returns events using pagination and sorting parameters"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Paginated events retrieved successfully"
    )
    public Page<EventResponse> getEvents(
            Pageable pageable) {

        return eventService.getEvents(pageable);
    }

    // =========================================================
    // GET EVENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get event by ID",
            description = "Returns detailed information about a specific event"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found"
            )
    })
    public EventResponse getEventById(
            @PathVariable Long id) {

        return eventService.getEventById(id);
    }

    // =========================================================
    // UPDATE EVENT
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @Operation(
            summary = "Update an event",
            description = "Updates an event. Organizers can update only events they own."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid event data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to update this event"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found"
            )
    })
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
    @Operation(
            summary = "Delete an event",
            description = "Deletes an event. Organizers can delete only events they own."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Event deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have permission to delete this event"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found"
            )
    })
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            Authentication authentication) {

        eventService.deleteEvent(
                id,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // DYNAMIC EVENT FILTER
    // =========================================================

    @GetMapping("/filter")
    @Operation(
            summary = "Filter events dynamically",
            description = "Filters events using optional keyword, category, location, price range, date range, pagination, and sorting"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered events retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter parameter"
            )
    })
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