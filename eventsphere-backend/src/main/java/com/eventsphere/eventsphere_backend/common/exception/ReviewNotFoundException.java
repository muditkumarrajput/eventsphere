package com.eventsphere.eventsphere_backend.common.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long reviewId) {
        super("Review with id " + reviewId + " not found");
    }
}