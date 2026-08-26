package com.eventsphere.eventsphere_backend.event.service;

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
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;


    // =========================================================
    // CREATE EVENT
    // =========================================================

    @Test
    void shouldCreateEvent() {

        String email = "organizer@test.com";

        User organizer = new User();
        organizer.setId(3L);
        organizer.setRole(Role.ORGANIZER);

        CreateEventRequest request = new CreateEventRequest();

        Event event = new Event();
        event.setId(8L);

        EventResponse response = EventResponse.builder()
                .id(8L)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(organizer));

        when(eventMapper.toEntity(request))
                .thenReturn(event);

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        EventResponse result =
                eventService.createEvent(request, email);

        assertEquals(8L, result.getId());

        assertEquals(
                organizer,
                event.getCreatedBy()
        );

        verify(eventRepository).save(event);
        verify(eventMapper).toEntity(request);
        verify(eventMapper).toResponse(event);
    }


    @Test
    void shouldThrowExceptionWhenOrganizerDoesNotExistDuringCreate() {

        String email = "unknown@test.com";

        CreateEventRequest request = new CreateEventRequest();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> eventService.createEvent(request, email)
        );

        verifyNoInteractions(eventRepository);
        verifyNoInteractions(eventMapper);
    }


    // =========================================================
    // GET ALL EVENTS
    // =========================================================

    @Test
    void shouldGetAllEvents() {

        Event event1 = new Event();
        event1.setId(1L);

        Event event2 = new Event();
        event2.setId(2L);

        EventResponse response1 =
                EventResponse.builder().id(1L).build();

        EventResponse response2 =
                EventResponse.builder().id(2L).build();

        when(eventRepository.findAll())
                .thenReturn(List.of(event1, event2));

        when(eventMapper.toResponse(event1))
                .thenReturn(response1);

        when(eventMapper.toResponse(event2))
                .thenReturn(response2);

        List<EventResponse> result =
                eventService.getAllEvents();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(eventRepository).findAll();
        verify(eventMapper).toResponse(event1);
        verify(eventMapper).toResponse(event2);
    }


    // =========================================================
    // GET MY EVENTS
    // =========================================================

    @Test
    void shouldGetMyEvents() {

        String email = "organizer@test.com";

        User organizer = new User();
        organizer.setId(3L);

        Event event = new Event();
        event.setId(8L);
        event.setCreatedBy(organizer);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(organizer));

        when(eventRepository.findByCreatedBy(organizer))
                .thenReturn(List.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        List<EventResponse> result =
                eventService.getMyEvents(email);

        assertEquals(1, result.size());
        assertEquals(8L, result.get(0).getId());

        verify(eventRepository)
                .findByCreatedBy(organizer);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistForMyEvents() {

        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> eventService.getMyEvents(email)
        );

        verifyNoInteractions(eventRepository);
    }


    // =========================================================
    // GET EVENT BY ID
    // =========================================================

    @Test
    void shouldGetEventById() {

        Long eventId = 8L;

        Event event = new Event();
        event.setId(eventId);

        EventResponse response =
                EventResponse.builder()
                        .id(eventId)
                        .build();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        EventResponse result =
                eventService.getEventById(eventId);

        assertEquals(eventId, result.getId());

        verify(eventRepository).findById(eventId);
        verify(eventMapper).toResponse(event);
    }


    @Test
    void shouldThrowExceptionWhenEventDoesNotExist() {

        Long eventId = 999L;

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        assertThrows(
                EventNotFoundException.class,
                () -> eventService.getEventById(eventId)
        );

        verify(eventRepository).findById(eventId);
        verifyNoInteractions(eventMapper);
    }


    // =========================================================
    // UPDATE EVENT
    // =========================================================

    @Test
    void shouldAllowOrganizerToUpdateOwnEvent() {

        Long eventId = 8L;
        String email = "organizer@test.com";

        User organizer = new User();
        organizer.setId(3L);
        organizer.setRole(Role.ORGANIZER);

        Event event = new Event();
        event.setId(eventId);
        event.setCreatedBy(organizer);

        UpdateEventRequest request = new UpdateEventRequest();
        request.setTitle("Updated Workshop");
        request.setDescription("Updated Description");
        request.setLocation("Mumbai");
        request.setEventDate(
                LocalDateTime.of(2026, 12, 30, 10, 0)
        );
        request.setCapacity(100);
        request.setTicketPrice(new BigDecimal("1499"));
        request.setCategory(EventCategory.WORKSHOP);

        EventResponse response =
                EventResponse.builder()
                        .id(eventId)
                        .title("Updated Workshop")
                        .build();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(organizer));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        EventResponse result =
                eventService.updateEvent(
                        eventId,
                        request,
                        email
                );

        assertEquals(
                "Updated Workshop",
                result.getTitle()
        );

        assertEquals(
                "Updated Workshop",
                event.getTitle()
        );

        assertEquals(
                "Mumbai",
                event.getLocation()
        );

        verify(eventRepository).save(event);
    }


    @Test
    void shouldAllowAdminToUpdateAnyEvent() {

        Long eventId = 8L;
        String email = "admin@test.com";

        User owner = new User();
        owner.setId(3L);
        owner.setRole(Role.ORGANIZER);

        User admin = new User();
        admin.setId(2L);
        admin.setRole(Role.ADMIN);

        Event event = new Event();
        event.setId(eventId);
        event.setCreatedBy(owner);

        UpdateEventRequest request = new UpdateEventRequest();
        request.setTitle("Admin Updated Event");
        request.setDescription("Updated by admin");
        request.setLocation("Delhi");
        request.setEventDate(
                LocalDateTime.of(2026, 12, 31, 10, 0)
        );
        request.setCapacity(200);
        request.setTicketPrice(new BigDecimal("999"));
        request.setCategory(EventCategory.CONFERENCE);

        EventResponse response =
                EventResponse.builder()
                        .id(eventId)
                        .title("Admin Updated Event")
                        .build();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(admin));

        when(eventRepository.save(event))
                .thenReturn(event);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        EventResponse result =
                eventService.updateEvent(
                        eventId,
                        request,
                        email
                );

        assertEquals(
                "Admin Updated Event",
                result.getTitle()
        );

        verify(eventRepository).save(event);
    }


    @Test
    void shouldRejectOrganizerUpdatingAnotherUsersEvent() {

        Long eventId = 8L;

        User owner = new User();
        owner.setId(3L);
        owner.setRole(Role.ORGANIZER);

        User anotherOrganizer = new User();
        anotherOrganizer.setId(4L);
        anotherOrganizer.setRole(Role.ORGANIZER);

        Event event = new Event();
        event.setId(eventId);
        event.setCreatedBy(owner);

        UpdateEventRequest request = new UpdateEventRequest();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findByEmail("organizer2@test.com"))
                .thenReturn(Optional.of(anotherOrganizer));

        assertThrows(
                EventOwnershipException.class,
                () -> eventService.updateEvent(
                        eventId,
                        request,
                        "organizer2@test.com"
                )
        );

        verify(eventRepository, never())
                .save(any(Event.class));
    }


    // =========================================================
    // DELETE EVENT
    // =========================================================

    @Test
    void shouldAllowOrganizerToDeleteOwnEvent() {

        Long eventId = 8L;
        String email = "organizer@test.com";

        User organizer = new User();
        organizer.setId(3L);
        organizer.setRole(Role.ORGANIZER);

        Event event = new Event();
        event.setId(eventId);
        event.setCreatedBy(organizer);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(organizer));

        eventService.deleteEvent(eventId, email);

        verify(eventRepository).delete(event);
    }


    @Test
    void shouldAllowAdminToDeleteAnyEvent() {

        Long eventId = 8L;
        String email = "admin@test.com";

        User owner = new User();
        owner.setId(3L);
        owner.setRole(Role.ORGANIZER);

        User admin = new User();
        admin.setId(2L);
        admin.setRole(Role.ADMIN);

        Event event = new Event();
        event.setId(eventId);
        event.setCreatedBy(owner);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(admin));

        eventService.deleteEvent(eventId, email);

        verify(eventRepository).delete(event);
    }


    @Test
    void shouldRejectOrganizerDeletingAnotherUsersEvent() {

        Long eventId = 8L;

        User owner = new User();
        owner.setId(3L);
        owner.setRole(Role.ORGANIZER);

        User anotherOrganizer = new User();
        anotherOrganizer.setId(4L);
        anotherOrganizer.setRole(Role.ORGANIZER);

        Event event = new Event();
        event.setId(eventId);
        event.setCreatedBy(owner);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(userRepository.findByEmail("organizer2@test.com"))
                .thenReturn(Optional.of(anotherOrganizer));

        assertThrows(
                EventOwnershipException.class,
                () -> eventService.deleteEvent(
                        eventId,
                        "organizer2@test.com"
                )
        );

        verify(eventRepository, never())
                .delete(any(Event.class));
    }


    // =========================================================
    // SEARCH EVENTS
    // =========================================================

    @Test
    void shouldSearchEvents() {

        Event event = new Event();
        event.setId(8L);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        when(eventRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        "Java",
                        "Java"
                ))
                .thenReturn(List.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        List<EventResponse> result =
                eventService.searchEvents("Java");

        assertEquals(1, result.size());
        assertEquals(
                "Java Workshop",
                result.get(0).getTitle()
        );

        verify(eventRepository)
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        "Java",
                        "Java"
                );
    }


    // =========================================================
    // CATEGORY
    // =========================================================

    @Test
    void shouldGetEventsByCategory() {

        Event event = new Event();
        event.setId(8L);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .build();

        when(eventRepository.findByCategory(
                EventCategory.WORKSHOP
        )).thenReturn(List.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        List<EventResponse> result =
                eventService.getEventsByCategory(
                        EventCategory.WORKSHOP
                );

        assertEquals(1, result.size());
        assertEquals(
                8L,
                result.get(0).getId()
        );
    }


    // =========================================================
    // LOCATION
    // =========================================================

    @Test
    void shouldGetEventsByLocation() {

        Event event = new Event();
        event.setId(8L);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .build();

        when(eventRepository
                .findByLocationContainingIgnoreCase("Mumbai"))
                .thenReturn(List.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        List<EventResponse> result =
                eventService.getEventsByLocation("Mumbai");

        assertEquals(1, result.size());
        assertEquals(
                8L,
                result.get(0).getId()
        );
    }


    // =========================================================
    // DATE
    // =========================================================

    @Test
    void shouldGetEventsByDate() {

        LocalDate date =
                LocalDate.of(2026, 12, 20);

        Event event = new Event();
        event.setId(8L);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .build();

        when(eventRepository.findByEventDateBetween(
                date.atStartOfDay(),
                date.atTime(
                        java.time.LocalTime.MAX
                )
        )).thenReturn(List.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        List<EventResponse> result =
                eventService.getEventsByDate(date);

        assertEquals(1, result.size());
        assertEquals(
                8L,
                result.get(0).getId()
        );
    }


    // =========================================================
    // PRICE RANGE
    // =========================================================

    @Test
    void shouldGetEventsByPriceRange() {

        BigDecimal minPrice =
                new BigDecimal("500");

        BigDecimal maxPrice =
                new BigDecimal("1500");

        Event event = new Event();
        event.setId(8L);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .build();

        when(eventRepository.findByTicketPriceBetween(
                minPrice,
                maxPrice
        )).thenReturn(List.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        List<EventResponse> result =
                eventService.getEventsByPriceRange(
                        minPrice,
                        maxPrice
                );

        assertEquals(1, result.size());
        assertEquals(
                8L,
                result.get(0).getId()
        );
    }


    // =========================================================
    // UPCOMING EVENTS
    // =========================================================

    @Test
    void shouldGetUpcomingEvents() {

        Event event = new Event();
        event.setId(8L);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .build();

        when(eventRepository
                .findByEventDateAfterOrderByEventDateAsc(
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(event));

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        List<EventResponse> result =
                eventService.getUpcomingEvents();

        assertEquals(1, result.size());
        assertEquals(
                8L,
                result.get(0).getId()
        );

        verify(eventRepository)
                .findByEventDateAfterOrderByEventDateAsc(
                        any(LocalDateTime.class)
                );
    }


    // =========================================================
    // PAGINATION
    // =========================================================

    @Test
    void shouldGetEventsWithPagination() {

        Pageable pageable =
                PageRequest.of(0, 5);

        Event event = new Event();
        event.setId(8L);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .build();

        Page<Event> eventPage =
                new PageImpl<>(
                        List.of(event),
                        pageable,
                        1
                );

        when(eventRepository.findAll(pageable))
                .thenReturn(eventPage);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        Page<EventResponse> result =
                eventService.getEvents(pageable);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                1,
                result.getContent().size()
        );

        assertEquals(
                8L,
                result.getContent().get(0).getId()
        );

        verify(eventRepository)
                .findAll(pageable);
    }


    // =========================================================
    // DYNAMIC EVENT FILTERING
    // =========================================================

    @Test
    void shouldFilterEvents() {

        String keyword = "Java";
        EventCategory category = EventCategory.WORKSHOP;
        String location = "Mumbai";

        BigDecimal minPrice =
                new BigDecimal("500");

        BigDecimal maxPrice =
                new BigDecimal("1500");

        LocalDateTime startDate =
                LocalDateTime.of(2026, 12, 1, 0, 0);

        LocalDateTime endDate =
                LocalDateTime.of(2026, 12, 31, 23, 59);

        Pageable pageable =
                PageRequest.of(0, 5);

        Event event = new Event();
        event.setId(8L);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .title("Java Workshop")
                        .build();

        Page<Event> eventPage =
                new PageImpl<>(
                        List.of(event),
                        pageable,
                        1
                );

        when(eventRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(eventPage);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        Page<EventResponse> result =
                eventService.filterEvents(
                        keyword,
                        category,
                        location,
                        minPrice,
                        maxPrice,
                        startDate,
                        endDate,
                        pageable
                );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                1,
                result.getContent().size()
        );

        assertEquals(
                8L,
                result.getContent().get(0).getId()
        );

        assertEquals(
                "Java Workshop",
                result.getContent().get(0).getTitle()
        );

        verify(eventRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verify(eventMapper)
                .toResponse(event);
    }


    @Test
    void shouldFilterEventsWithoutOptionalFilters() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Event event = new Event();
        event.setId(8L);

        EventResponse response =
                EventResponse.builder()
                        .id(8L)
                        .build();

        Page<Event> eventPage =
                new PageImpl<>(
                        List.of(event),
                        pageable,
                        1
                );

        when(eventRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(eventPage);

        when(eventMapper.toResponse(event))
                .thenReturn(response);

        Page<EventResponse> result =
                eventService.filterEvents(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        pageable
                );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                8L,
                result.getContent().get(0).getId()
        );

        verify(eventRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verify(eventMapper)
                .toResponse(event);
    }
}
