package com.eventsphere.eventsphere_backend.common.exception;

public class ReviewAlreadyExistsException extends RuntimeException {

    public ReviewAlreadyExistsException(Long eventId) {
        super("You have already reviewed event with id " + eventId);
    }
}