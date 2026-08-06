package com.eventsphere.eventsphere_backend.common.exception;

public class ReviewOwnershipException extends RuntimeException {

    public ReviewOwnershipException() {
        super("You can only modify your own review");
    }
}