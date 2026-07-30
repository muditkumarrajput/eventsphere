package com.eventsphere.eventsphere_backend.event.service;

import com.eventsphere.eventsphere_backend.common.exception.EventNotFoundException;
import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Create Event
    public EventResponse createEvent(CreateEventRequest request) {

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .eventDate(request.getEventDate())
                .capacity(request.getCapacity())
                .ticketPrice(request.getTicketPrice())
                .category(request.getCategory())
                .build();

        Event savedEvent = eventRepository.save(event);

        return mapToResponse(savedEvent);
    }

    // Get All Events
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get Event By Id
    public EventResponse getEventById(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        return mapToResponse(event);
    }

    // Entity -> Response DTO
    private EventResponse mapToResponse(Event event) {

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .eventDate(event.getEventDate())
                .capacity(event.getCapacity())
                .ticketPrice(event.getTicketPrice())
                .category(event.getCategory())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}