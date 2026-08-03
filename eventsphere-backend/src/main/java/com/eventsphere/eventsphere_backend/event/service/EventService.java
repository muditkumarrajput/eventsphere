package com.eventsphere.eventsphere_backend.event.service;

import com.eventsphere.eventsphere_backend.common.exception.EventNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.EventOwnershipException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.mapper.EventMapper;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository,
            EventMapper eventMapper) {

        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventMapper = eventMapper;
    }

    // Create Event
    public EventResponse createEvent(
            CreateEventRequest request,
            String email) {

        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Event event = eventMapper.toEntity(request);

        event.setCreatedBy(organizer);

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

    // Get My Events
    public List<EventResponse> getMyEvents(String email) {

        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return eventRepository.findByCreatedBy(organizer)
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get Event By Id
    public EventResponse getEventById(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(id));

        return eventMapper.toResponse(event);
    }

    // Update Event
    public EventResponse updateEvent(
            Long id,
            UpdateEventRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(id));

        validateOwnership(event, user);

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
    public void deleteEvent(Long id, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(id));

        validateOwnership(event, user);

        eventRepository.delete(event);
    }

    // Ownership Validation
    private void validateOwnership(Event event, User user) {

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (!event.getCreatedBy().getId().equals(user.getId())) {
            throw new EventOwnershipException();
        }
    }
}