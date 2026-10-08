package com.eventsphere.eventsphere_backend.common.exception;

public class UserPhoneAlreadyExistsException extends RuntimeException {

    public UserPhoneAlreadyExistsException(String phoneNumber) {
        super("User with phone number '" + phoneNumber + "' already exists");
    }
}
