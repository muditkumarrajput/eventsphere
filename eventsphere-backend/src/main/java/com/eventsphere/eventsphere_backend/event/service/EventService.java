package com.eventsphere.eventsphere_backend.event.service;

import com.eventsphere.eventsphere_backend.common.exception.EventNotFoundException;
import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.mapper.EventMapper;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository,
                        EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    // Create Event
    public EventResponse createEvent(CreateEventRequest request) {

        Event event = eventMapper.toEntity(request);

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    // Get All Events
    public List<EventResponse> getAllEvents() {

        return eventRepository.findAll()
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get Event By Id
    public EventResponse getEventById(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        return eventMapper.toResponse(event);
    }

    // Update Event
    public EventResponse updateEvent(Long id, UpdateEventRequest request) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setCapacity(request.getCapacity());
        event.setTicketPrice(request.getTicketPrice());
        event.setCategory(request.getCategory());

        Event updatedEvent = eventRepository.save(event);

        return eventMapper.toResponse(updatedEvent);
    }

    // Delete Event
    public void deleteEvent(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        eventRepository.delete(event);
    }
}