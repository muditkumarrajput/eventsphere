package com.eventsphere.eventsphere_backend.common.exception;

public class EventCapacityExceededException extends RuntimeException {

    public EventCapacityExceededException() {
        super("Requested tickets exceed available event capacity.");
    }

}