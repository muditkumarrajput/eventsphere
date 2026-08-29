package com.eventsphere.eventsphere_backend.booking.mapper;

import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.entity.Booking;
import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    private final BookingMapper bookingMapper = new BookingMapper();

    @Test
    void toResponse_shouldMapAllFields() {

        LocalDateTime bookingDate =
                LocalDateTime.of(2026, 8, 29, 10, 30);

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 29, 10, 0);

        LocalDateTime updatedAt =
                LocalDateTime.of(2026, 8, 29, 10, 15);

        User user = User.builder()
                .id(10L)
                .build();

        Event event = Event.builder()
                .id(20L)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .bookingReference("BOOK-12345")
                .user(user)
                .event(event)
                .numberOfTickets(3)
                .totalAmount(new BigDecimal("2997.00"))
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingDate(bookingDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        BookingResponse result = bookingMapper.toResponse(booking);

        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("BOOK-12345", result.getBookingReference());
        assertEquals(10L, result.getUserId());
        assertEquals(20L, result.getEventId());
        assertEquals(3, result.getNumberOfTickets());
        assertEquals(
                new BigDecimal("2997.00"),
                result.getTotalAmount()
        );
        assertEquals(
                BookingStatus.CONFIRMED,
                result.getBookingStatus()
        );
        assertEquals(bookingDate, result.getBookingDate());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
    }
}