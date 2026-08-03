package com.eventsphere.eventsphere_backend.common.exception;

public class EventOwnershipException extends RuntimeException {

    public EventOwnershipException() {
        super("You are not allowed to modify this event.");
    }

}