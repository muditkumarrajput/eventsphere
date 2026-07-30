package com.eventsphere.eventsphere_backend.event.controller;

import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.service.EventService;
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
    public EventResponse createEvent(@RequestBody CreateEventRequest request) {
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
}