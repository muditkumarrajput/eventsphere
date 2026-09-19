package com.eventsphere.eventsphere_backend.dashboard.dto;

import com.eventsphere.eventsphere_backend.event.entity.EventStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventInsightResponse {

    private Long eventId;

    private String title;

    private Integer capacity;

    private EventStatus status;

    private Long ticketsSold;

    private Integer remainingSeats;

    private Double occupancyPercentage;

    private BigDecimal revenue;
}