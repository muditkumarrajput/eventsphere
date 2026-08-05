package com.eventsphere.eventsphere_backend.favorite.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponse {

    private Long id;

    private Long eventId;

    private String eventTitle;

    private String eventLocation;

    private LocalDateTime eventDate;

    private LocalDateTime createdAt;
}