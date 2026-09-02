package com.eventsphere.eventsphere_backend.event.service;

import com.eventsphere.eventsphere_backend.booking.repository.BookingRepository;
import com.eventsphere.eventsphere_backend.common.exception.EventHasBookingsException;
import com.eventsphere.eventsphere_backend.common.exception.EventNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.EventOwnershipException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.event.mapper.EventMapper;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.event.specification.EventSpecification;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final EventMapper eventMapper;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            EventMapper eventMapper) {

        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.eventMapper = eventMapper;
    }

    // =========================================================
    // CREATE EVENT
    // =========================================================

    public EventResponse createEvent(
            CreateEventRequest request,
            String email) {

        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Event event = eventMapper.toEntity(request);

        // Store the user who created the event
        event.setCreatedBy(organizer);

        Event savedEvent = eventRepository.save(event);

        return eventMapper.toResponse(savedEvent);
    }

    // =========================================================
    // GET ALL EVENTS
    // =========================================================

    public List<EventResponse> getAllEvents() {

        return eventRepository.findAll()
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET MY EVENTS
    // =========================================================

    public List<EventResponse> getMyEvents(String email) {

        User organizer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return eventRepository.findByCreatedBy(organizer)
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET EVENT BY ID
    // =========================================================

    public EventResponse getEventById(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(id));

        return eventMapper.toResponse(event);
    }

    // =========================================================
    // UPDATE EVENT
    // =========================================================

    public EventResponse updateEvent(
            Long id,
            UpdateEventRequest request,
            String email) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(id));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        // Check whether the user is allowed to modify this event
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

    // =========================================================
    // DELETE EVENT
    // =========================================================

    @Transactional
    public void deleteEvent(
            Long id,
            String email) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(id));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        // Check ownership before checking whether deletion is allowed
        validateOwnership(event, user);

        /*
         * Do not physically delete an event that has bookings.
         *
         * Bookings represent historical transactions and may
         * have associated payment records.
         */
        if (bookingRepository.existsByEvent(event)) {
            throw new EventHasBookingsException(id);
        }

        eventRepository.delete(event);
    }

    // =========================================================
    // SEARCH EVENTS
    // =========================================================

    public List<EventResponse> searchEvents(String keyword) {

        return eventRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        keyword,
                        keyword
                )
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // FILTER BY CATEGORY
    // =========================================================

    public List<EventResponse> getEventsByCategory(
            EventCategory category) {

        return eventRepository.findByCategory(category)
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // FILTER BY LOCATION
    // =========================================================

    public List<EventResponse> getEventsByLocation(
            String location) {

        return eventRepository
                .findByLocationContainingIgnoreCase(location)
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // FILTER BY DATE
    // =========================================================

    public List<EventResponse> getEventsByDate(
            LocalDate date) {

        LocalDateTime start = date.atStartOfDay();

        LocalDateTime end = date.atTime(LocalTime.MAX);

        return eventRepository
                .findByEventDateBetween(start, end)
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // FILTER BY PRICE RANGE
    // =========================================================

    public List<EventResponse> getEventsByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return eventRepository
                .findByTicketPriceBetween(
                        minPrice,
                        maxPrice
                )
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // UPCOMING EVENTS
    // =========================================================

    public List<EventResponse> getUpcomingEvents() {

        return eventRepository
                .findByEventDateAfterOrderByEventDateAsc(
                        LocalDateTime.now()
                )
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // PAGINATION + SORTING
    // =========================================================

    public Page<EventResponse> getEvents(
            Pageable pageable) {

        return eventRepository
                .findAll(pageable)
                .map(eventMapper::toResponse);
    }

    // =========================================================
    // OWNERSHIP VALIDATION
    // =========================================================

    private void validateOwnership(
            Event event,
            User user) {

        // ADMIN can modify any event
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        // ORGANIZER can modify only their own events
        if (event.getCreatedBy() == null ||
                !event.getCreatedBy()
                        .getId()
                        .equals(user.getId())) {

            throw new EventOwnershipException();
        }
    }

    // =========================================================
    // DYNAMIC EVENT FILTERING
    // =========================================================

    public Page<EventResponse> filterEvents(
            String keyword,
            EventCategory category,
            String location,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        Specification<Event> specification = Specification.allOf(
                EventSpecification.keywordContains(keyword),
                EventSpecification.hasCategory(category),
                EventSpecification.hasLocation(location),
                EventSpecification.priceGreaterThanOrEqual(minPrice),
                EventSpecification.priceLessThanOrEqual(maxPrice),
                EventSpecification.eventDateAfter(startDate),
                EventSpecification.eventDateBefore(endDate)
        );

        return eventRepository
                .findAll(specification, pageable)
                .map(eventMapper::toResponse);
    }
}