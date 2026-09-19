package com.eventsphere.eventsphere_backend.organizer.dto;

import com.eventsphere.eventsphere_backend.organizer.entity.OrganizerRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerRequestResponse {

    private Long id;

    private Long userId;

    private String userName;

    private String userEmail;

    private String reason;

    private OrganizerRequestStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    private Long reviewedById;

    private String reviewedByName;
}