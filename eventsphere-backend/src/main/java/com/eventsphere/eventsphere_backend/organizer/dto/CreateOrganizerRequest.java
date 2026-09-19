package com.eventsphere.eventsphere_backend.organizer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrganizerRequest {

    @NotBlank(message = "Reason is required")
    @Size(
            max = 1000,
            message = "Reason must not exceed 1000 characters"
    )
    private String reason;
}