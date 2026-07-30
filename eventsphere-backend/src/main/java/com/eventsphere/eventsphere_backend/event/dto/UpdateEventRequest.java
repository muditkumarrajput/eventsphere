package com.eventsphere.eventsphere_backend.event.dto;

import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDate;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than 0")
    private Integer capacity;

    @NotNull(message = "Ticket price is required")
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Ticket price cannot be negative"
    )
    private BigDecimal ticketPrice;

    @NotNull(message = "Category is required")
    private EventCategory category;
}