package com.eventsphere.eventsphere_backend.dashboard.dto;

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

    private Long ticketsSold;

    private Integer remainingSeats;

    private Double occupancyPercentage;

    private BigDecimal revenue;
}