package com.eventsphere.eventsphere_backend.common.exception;

public class UserOwnershipException extends RuntimeException {

    public UserOwnershipException() {
        super("You are not allowed to modify this user.");
    }
}