package com.eventsphere.eventsphere_backend.event.controller;

import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Create Event
    @PostMapping
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request) {
        return eventService.createEvent(request);
    }

    // Get All Events
    @GetMapping
    public List<EventResponse> getAllEvents() {
        return eventService.getAllEvents();
    }

    // Get Event By Id
    @GetMapping("/{id}")
    public EventResponse getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    // Update Event
    @PutMapping("/{id}")
    public EventResponse updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {

        return eventService.updateEvent(id, request);
    }

    // Delete Event
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {

        eventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }
}