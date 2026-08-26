package com.eventsphere.eventsphere_backend.common.exception;

import com.eventsphere.eventsphere_backend.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // EVENT NOT FOUND EXCEPTION
    // =========================================================

    @ExceptionHandler(EventNotFoundException.class)
    public ErrorResponse handleEventNotFoundException(
            EventNotFoundException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // REVIEW OWNERSHIP EXCEPTION
    // =========================================================

    @ExceptionHandler(ReviewOwnershipException.class)
    public ErrorResponse handleReviewOwnershipException(
            ReviewOwnershipException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // INVALID REQUEST PARAMETER / ENUM CONVERSION EXCEPTION
    // =========================================================

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ErrorResponse handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = "Invalid value '" + ex.getValue()
                + "' for parameter '" + ex.getName() + "'";

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // BOOKING NOT FOUND EXCEPTION
    // =========================================================

    @ExceptionHandler(BookingNotFoundException.class)
    public ErrorResponse handleBookingNotFoundException(
            BookingNotFoundException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // BOOKING ALREADY CANCELLED EXCEPTION
    // =========================================================

    @ExceptionHandler(BookingAlreadyCancelledException.class)
    public ErrorResponse handleBookingAlreadyCancelledException(
            BookingAlreadyCancelledException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // REVIEW ALREADY EXISTS EXCEPTION
    // =========================================================

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ErrorResponse handleReviewAlreadyExistsException(
            ReviewAlreadyExistsException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // PAYMENT ALREADY EXISTS EXCEPTION
    // =========================================================

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    public ErrorResponse handlePaymentAlreadyExistsException(
            PaymentAlreadyExistsException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // USER EMAIL ALREADY EXISTS EXCEPTION
    // =========================================================

    @ExceptionHandler(UserEmailAlreadyExistsException.class)
    public ErrorResponse handleUserEmailAlreadyExistsException(
            UserEmailAlreadyExistsException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // EVENT CAPACITY EXCEEDED EXCEPTION
    // =========================================================

    @ExceptionHandler(EventCapacityExceededException.class)
    public ErrorResponse handleEventCapacityExceededException(
            EventCapacityExceededException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // VALIDATION EXCEPTION
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // AUTHORIZATION DENIED EXCEPTION
    // =========================================================

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ErrorResponse handleAuthorizationDeniedException(
            AuthorizationDeniedException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Access Denied")
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // EVENT OWNERSHIP EXCEPTION
    // =========================================================

    @ExceptionHandler(EventOwnershipException.class)
    public ErrorResponse handleEventOwnershipException(
            EventOwnershipException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // USER HAS EVENTS EXCEPTION
    // =========================================================

    @ExceptionHandler(UserHasEventsException.class)
    public ErrorResponse handleUserHasEventsException(
            UserHasEventsException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // REVIEW NOT FOUND EXCEPTION
    // =========================================================

    @ExceptionHandler(ReviewNotFoundException.class)
    public ErrorResponse handleReviewNotFoundException(
            ReviewNotFoundException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // PAYMENT NOT FOUND EXCEPTION
    // =========================================================

    @ExceptionHandler(PaymentNotFoundException.class)
    public ErrorResponse handlePaymentNotFoundException(
            PaymentNotFoundException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // NOTIFICATION NOT FOUND EXCEPTION
    // =========================================================

    @ExceptionHandler(NotificationNotFoundException.class)
    public ErrorResponse handleNotificationNotFoundException(
            NotificationNotFoundException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // PAYMENT STATE TRANSITION EXCEPTION
    // =========================================================

    @ExceptionHandler(PaymentStateTransitionException.class)
    public ErrorResponse handlePaymentStateTransitionException(
            PaymentStateTransitionException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // REVIEW NOT ALLOWED EXCEPTION
    // =========================================================

    @ExceptionHandler(ReviewNotAllowedException.class)
    public ErrorResponse handleReviewNotAllowedException(
            ReviewNotAllowedException ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }

    // =========================================================
    // GENERIC EXCEPTION
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();
    }
}