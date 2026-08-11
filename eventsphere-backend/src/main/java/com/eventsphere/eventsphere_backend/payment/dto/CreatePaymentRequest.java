package com.eventsphere.eventsphere_backend.payment.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(

        @NotNull(message = "Booking ID is required")
        Long bookingId

) {
}