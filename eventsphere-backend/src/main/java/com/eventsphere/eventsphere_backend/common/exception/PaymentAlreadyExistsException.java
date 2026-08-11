package com.eventsphere.eventsphere_backend.common.exception;

public class PaymentAlreadyExistsException extends RuntimeException {

    public PaymentAlreadyExistsException(Long bookingId) {
        super("Payment already exists for booking with id: " + bookingId);
    }
}