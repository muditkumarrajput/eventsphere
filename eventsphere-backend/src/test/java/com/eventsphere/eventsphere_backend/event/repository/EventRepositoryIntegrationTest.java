package com.eventsphere.eventsphere_backend.event.repository;

import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.integration.AbstractPostgresIntegrationTest;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EventRepositoryIntegrationTest
        extends AbstractPostgresIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User organizer;

    @BeforeEach
    void setUp() {

        eventRepository.deleteAll();
        userRepository.deleteAll();

        organizer = new User();
        organizer.setEmail("organizer@test.com");
        organizer.setRole(Role.ORGANIZER);

        organizer = userRepository.save(organizer);
    }


    @Test
    void shouldSaveAndFindEventById() {

        Event event = new Event();

        event.setTitle("Java Workshop");
        event.setDescription("Spring Boot workshop");
        event.setLocation("Mumbai");
        event.setEventDate(
                LocalDateTime.of(
                        2026,
                        12,
                        20,
                        10,
                        0
                )
        );
        event.setCapacity(100);
        event.setTicketPrice(
                new BigDecimal("999")
        );
        event.setCategory(
                EventCategory.WORKSHOP
        );
        event.setCreatedBy(organizer);

        Event savedEvent =
                eventRepository.save(event);

        Event result =
                eventRepository
                        .findById(savedEvent.getId())
                        .orElseThrow();

        assertEquals(
                "Java Workshop",
                result.getTitle()
        );

        assertEquals(
                "Mumbai",
                result.getLocation()
        );

        assertEquals(
                EventCategory.WORKSHOP,
                result.getCategory()
        );

        assertEquals(
                organizer.getId(),
                result.getCreatedBy().getId()
        );
    }


    @Test
    void shouldFindEventsByCategory() {

        Event event = new Event();

        event.setTitle("Spring Boot Workshop");
        event.setDescription("Backend development");
        event.setLocation("Delhi");
        event.setEventDate(
                LocalDateTime.of(
                        2026,
                        12,
                        25,
                        10,
                        0
                )
        );
        event.setCapacity(50);
        event.setTicketPrice(
                new BigDecimal("500")
        );
        event.setCategory(
                EventCategory.WORKSHOP
        );
        event.setCreatedBy(organizer);

        eventRepository.save(event);

        List<Event> result =
                eventRepository.findByCategory(
                        EventCategory.WORKSHOP
                );

        assertFalse(result.isEmpty());

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "Spring Boot Workshop",
                result.get(0).getTitle()
        );
    }


    @Test
    void shouldFindEventsByLocation() {

        Event event = new Event();

        event.setTitle("Java Conference");
        event.setDescription("Java conference");
        event.setLocation("Mumbai");
        event.setEventDate(
                LocalDateTime.of(
                        2026,
                        12,
                        28,
                        10,
                        0
                )
        );
        event.setCapacity(200);
        event.setTicketPrice(
                new BigDecimal("1500")
        );
        event.setCategory(
                EventCategory.CONFERENCE
        );
        event.setCreatedBy(organizer);

        eventRepository.save(event);

        List<Event> result =
                eventRepository
                        .findByLocationContainingIgnoreCase(
                                "mumbai"
                        );

        assertFalse(result.isEmpty());

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "Java Conference",
                result.get(0).getTitle()
        );
    }
}
