package com.eventsphere.eventsphere_backend.common.exception;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long id) {
        super("Booking with id " + id + " not found.");
    }


}