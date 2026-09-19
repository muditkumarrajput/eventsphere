package com.eventsphere.eventsphere_backend.booking.dto;

import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;

    private String bookingReference;

    private Long userId;

    private String userName;

    private String userEmail;

    private Long eventId;

    private String eventTitle;

    private String eventLocation;

    private LocalDateTime eventDate;

    private Integer numberOfTickets;

    private BigDecimal totalAmount;

    private BookingStatus bookingStatus;

    private LocalDateTime bookingDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}