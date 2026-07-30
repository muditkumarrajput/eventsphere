package com.eventsphere.eventsphere_backend.event.mapper;

import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(CreateEventRequest request) {

        return Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .eventDate(request.getEventDate())
                .capacity(request.getCapacity())
                .ticketPrice(request.getTicketPrice())
                .category(request.getCategory())
                .build();
    }

    public EventResponse toResponse(Event event) {

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .eventDate(event.getEventDate())
                .capacity(event.getCapacity())
                .ticketPrice(event.getTicketPrice())
                .category(event.getCategory())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }

}