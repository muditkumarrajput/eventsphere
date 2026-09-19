package com.eventsphere.eventsphere_backend.common.exception;

public class OrganizerRequestAlreadyProcessedException
        extends RuntimeException {

    public OrganizerRequestAlreadyProcessedException(String message) {
        super(message);
    }
}