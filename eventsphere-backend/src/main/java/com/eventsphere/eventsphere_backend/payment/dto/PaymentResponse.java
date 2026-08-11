package com.eventsphere.eventsphere_backend.payment.dto;

import com.eventsphere.eventsphere_backend.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    private Long id;

    private String paymentReference;

    private Long bookingId;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;

    private LocalDateTime paymentDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}