package com.eventsphere.eventsphere_backend.booking.mapper;

import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUser().getId())
                .userName(booking.getUser().getName())
                .userEmail(booking.getUser().getEmail())
                .eventId(booking.getEvent().getId())

                // Event information
                .eventTitle(booking.getEvent().getTitle())
                .eventLocation(booking.getEvent().getLocation())
                .eventDate(booking.getEvent().getEventDate())

                .numberOfTickets(booking.getNumberOfTickets())
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus())
                .bookingDate(booking.getBookingDate())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}