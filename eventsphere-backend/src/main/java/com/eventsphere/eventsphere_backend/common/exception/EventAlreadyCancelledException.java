package com.eventsphere.eventsphere_backend.common.exception;

public class EventAlreadyCancelledException extends RuntimeException {

    public EventAlreadyCancelledException(Long eventId) {
        super(
                "Event with id "
                        + eventId
                        + " has already been cancelled."
        );
    }
}