package com.eventsphere.eventsphere_backend.common.exception;

public class BookingAlreadyCancelledException extends RuntimeException {

    public BookingAlreadyCancelledException(Long bookingId) {
        super("Booking with id " + bookingId + " is already cancelled");
    }
}