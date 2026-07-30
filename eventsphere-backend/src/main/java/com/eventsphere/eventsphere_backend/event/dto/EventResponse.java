package com.eventsphere.eventsphere_backend.event.dto;

import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long id;

    private String title;

    private String description;

    private String location;

    private LocalDateTime eventDate;

    private Integer capacity;

    private BigDecimal ticketPrice;

    private EventCategory category;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}