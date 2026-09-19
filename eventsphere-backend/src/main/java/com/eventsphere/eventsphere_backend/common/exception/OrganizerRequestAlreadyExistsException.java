package com.eventsphere.eventsphere_backend.common.exception;

public class OrganizerRequestAlreadyExistsException
        extends RuntimeException {

    public OrganizerRequestAlreadyExistsException(String message) {
        super(message);
    }
}