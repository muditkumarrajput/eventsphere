package com.eventsphere.eventsphere_backend.common.exception;

public class EventHasBookingsException extends RuntimeException {

    private final Long eventId;

    public EventHasBookingsException(Long eventId) {
        super("Event with id " + eventId + " cannot be deleted because it has bookings.");
        this.eventId = eventId;
    }

    public Long getEventId() {
        return eventId;
    }
}