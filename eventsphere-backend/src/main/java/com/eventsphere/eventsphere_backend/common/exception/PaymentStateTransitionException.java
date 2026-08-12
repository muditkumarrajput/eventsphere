package com.eventsphere.eventsphere_backend.common.exception;

public class PaymentStateTransitionException extends RuntimeException {

    public PaymentStateTransitionException(
            Long paymentId,
            String currentStatus,
            String requestedStatus) {

        super("Payment with id " + paymentId
                + " cannot change from "
                + currentStatus
                + " to "
                + requestedStatus);
    }
}