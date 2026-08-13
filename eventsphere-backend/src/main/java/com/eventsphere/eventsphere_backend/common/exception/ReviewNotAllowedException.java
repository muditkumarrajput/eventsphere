package com.eventsphere.eventsphere_backend.common.exception;

public class ReviewNotAllowedException extends RuntimeException {

    public ReviewNotAllowedException(Long eventId) {
        super("You can only review an event after completing a confirmed booking. Event id: " + eventId);
    }
}