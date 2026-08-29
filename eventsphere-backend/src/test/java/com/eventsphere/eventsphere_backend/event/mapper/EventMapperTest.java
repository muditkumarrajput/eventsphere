package com.eventsphere.eventsphere_backend.event.mapper;

import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {

    private final EventMapper eventMapper = new EventMapper();

    @Test
    void toEntity_shouldMapAllFields() {

        LocalDateTime eventDate =
                LocalDateTime.of(2026, 10, 15, 18, 30);

        CreateEventRequest request = CreateEventRequest.builder()
                .title("Java Conference")
                .description("Backend development conference")
                .location("Lucknow")
                .eventDate(eventDate)
                .capacity(500)
                .ticketPrice(new BigDecimal("999.00"))
                .category(EventCategory.CONFERENCE)
                .build();

        Event result = eventMapper.toEntity(request);

        assertNotNull(result);

        assertEquals("Java Conference", result.getTitle());
        assertEquals(
                "Backend development conference",
                result.getDescription()
        );
        assertEquals("Lucknow", result.getLocation());
        assertEquals(eventDate, result.getEventDate());
        assertEquals(500, result.getCapacity());
        assertEquals(
                new BigDecimal("999.00"),
                result.getTicketPrice()
        );
        assertEquals(EventCategory.CONFERENCE, result.getCategory());
    }

    @Test
    void toResponse_shouldMapAllFields() {

        LocalDateTime eventDate =
                LocalDateTime.of(2026, 10, 15, 18, 30);

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 8, 25, 12, 0);

        Event event = Event.builder()
                .id(1L)
                .title("Java Conference")
                .description("Backend development conference")
                .location("Lucknow")
                .eventDate(eventDate)
                .capacity(500)
                .ticketPrice(new BigDecimal("999.00"))
                .category(EventCategory.CONFERENCE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        EventResponse result = eventMapper.toResponse(event);

        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("Java Conference", result.getTitle());
        assertEquals(
                "Backend development conference",
                result.getDescription()
        );
        assertEquals("Lucknow", result.getLocation());
        assertEquals(eventDate, result.getEventDate());
        assertEquals(500, result.getCapacity());
        assertEquals(
                new BigDecimal("999.00"),
                result.getTicketPrice()
        );
        assertEquals(EventCategory.CONFERENCE, result.getCategory());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
    }
}