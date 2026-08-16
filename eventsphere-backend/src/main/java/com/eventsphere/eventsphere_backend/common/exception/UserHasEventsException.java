package com.eventsphere.eventsphere_backend.common.exception;

public class UserHasEventsException extends RuntimeException {

    public UserHasEventsException(Long userId) {
        super("User with id " + userId +
                " cannot be deleted because they still own events.");
    }
}