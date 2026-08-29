package com.eventsphere.eventsphere_backend.common.exception;

import com.eventsphere.eventsphere_backend.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {

        handler = new GlobalExceptionHandler();

        request = mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/test");
    }


    // =========================================================
    // EVENT NOT FOUND
    // =========================================================

    @Test
    void shouldHandleEventNotFoundException() {

        EventNotFoundException exception =
                new EventNotFoundException(10L);

        ResponseEntity<ErrorResponse> response =
                handler.handleEventNotFoundException(exception, request);

        assertErrorResponse(
                response,
                404,
                "Not Found",
                "Event with id 10 not found.",
                "/api/test"
        );
    }


    // =========================================================
    // REVIEW OWNERSHIP
    // =========================================================

    @Test
    void shouldHandleReviewOwnershipException() {

        ReviewOwnershipException exception =
                new ReviewOwnershipException();

        ResponseEntity<ErrorResponse> response =
                handler.handleReviewOwnershipException(exception, request);

        assertErrorResponse(
                response,
                403,
                "Forbidden",
                "You can only modify your own review",
                "/api/test"
        );
    }


    // =========================================================
    // INVALID REQUEST PARAMETER / ENUM
    // =========================================================

    @Test
    void shouldHandleMethodArgumentTypeMismatchException() {

        MethodArgumentTypeMismatchException exception =
                mock(MethodArgumentTypeMismatchException.class);

        when(exception.getValue())
                .thenReturn("INVALID_CATEGORY");

        when(exception.getName())
                .thenReturn("category");

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentTypeMismatchException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                400,
                "Bad Request",
                "Invalid value 'INVALID_CATEGORY' for parameter 'category'",
                "/api/test"
        );
    }


    // =========================================================
    // BOOKING NOT FOUND
    // =========================================================

    @Test
    void shouldHandleBookingNotFoundException() {

        BookingNotFoundException exception =
                new BookingNotFoundException(20L);

        ResponseEntity<ErrorResponse> response =
                handler.handleBookingNotFoundException(exception, request);

        assertErrorResponse(
                response,
                404,
                "Not Found",
                "Booking with id 20 not found.",
                "/api/test"
        );
    }


    // =========================================================
    // BOOKING ALREADY CANCELLED
    // =========================================================

    @Test
    void shouldHandleBookingAlreadyCancelledException() {

        BookingAlreadyCancelledException exception =
                new BookingAlreadyCancelledException(20L);

        ResponseEntity<ErrorResponse> response =
                handler.handleBookingAlreadyCancelledException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                409,
                "Conflict",
                "Booking with id 20 is already cancelled",
                "/api/test"
        );
    }


    // =========================================================
    // REVIEW ALREADY EXISTS
    // =========================================================

    @Test
    void shouldHandleReviewAlreadyExistsException() {

        ReviewAlreadyExistsException exception =
                new ReviewAlreadyExistsException(30L);

        ResponseEntity<ErrorResponse> response =
                handler.handleReviewAlreadyExistsException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                409,
                "Conflict",
                "You have already reviewed event with id 30",
                "/api/test"
        );
    }


    // =========================================================
    // PAYMENT ALREADY EXISTS
    // =========================================================

    @Test
    void shouldHandlePaymentAlreadyExistsException() {

        PaymentAlreadyExistsException exception =
                new PaymentAlreadyExistsException(40L);

        ResponseEntity<ErrorResponse> response =
                handler.handlePaymentAlreadyExistsException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                409,
                "Conflict",
                "Payment already exists for booking with id: 40",
                "/api/test"
        );
    }


    // =========================================================
    // USER EMAIL ALREADY EXISTS
    // =========================================================

    @Test
    void shouldHandleUserEmailAlreadyExistsException() {

        UserEmailAlreadyExistsException exception =
                new UserEmailAlreadyExistsException(
                        "test@example.com"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleUserEmailAlreadyExistsException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                409,
                "Conflict",
                "User with email 'test@example.com' already exists",
                "/api/test"
        );
    }


    // =========================================================
    // EVENT CAPACITY EXCEEDED
    // =========================================================

    @Test
    void shouldHandleEventCapacityExceededException() {

        EventCapacityExceededException exception =
                new EventCapacityExceededException();

        ResponseEntity<ErrorResponse> response =
                handler.handleEventCapacityExceededException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                409,
                "Conflict",
                "Requested tickets exceed available event capacity.",
                "/api/test"
        );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    @Test
    void shouldHandleValidationException() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError fieldError =
                new FieldError(
                        "createEventRequest",
                        "title",
                        "Title is required"
                );

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldError())
                .thenReturn(fieldError);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                400,
                "Bad Request",
                "Title is required",
                "/api/test"
        );
    }


    // =========================================================
    // VALIDATION - NO FIELD ERROR
    // =========================================================

    @Test
    void shouldHandleValidationExceptionWhenNoFieldErrorExists() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldError())
                .thenReturn(null);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                400,
                "Bad Request",
                "Validation failed",
                "/api/test"
        );
    }


    // =========================================================
    // AUTHORIZATION DENIED
    // =========================================================

    @Test
    void shouldHandleAuthorizationDeniedException() {

        AuthorizationDeniedException exception =
                mock(AuthorizationDeniedException.class);

        ResponseEntity<ErrorResponse> response =
                handler.handleAuthorizationDeniedException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                403,
                "Forbidden",
                "You do not have permission to access this resource",
                "/api/test"
        );
    }


    // =========================================================
    // EVENT OWNERSHIP
    // =========================================================

    @Test
    void shouldHandleEventOwnershipException() {

        EventOwnershipException exception =
                new EventOwnershipException();

        ResponseEntity<ErrorResponse> response =
                handler.handleEventOwnershipException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                403,
                "Forbidden",
                "You are not allowed to modify this event.",
                "/api/test"
        );
    }


    // =========================================================
    // USER HAS EVENTS
    // =========================================================

    @Test
    void shouldHandleUserHasEventsException() {

        UserHasEventsException exception =
                new UserHasEventsException(50L);

        ResponseEntity<ErrorResponse> response =
                handler.handleUserHasEventsException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                409,
                "Conflict",
                "User with id 50 cannot be deleted because they still own events.",
                "/api/test"
        );
    }


    // =========================================================
    // USER NOT FOUND - ID
    // =========================================================

    @Test
    void shouldHandleUserNotFoundExceptionById() {

        UserNotFoundException exception =
                new UserNotFoundException(60L);

        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFoundException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                404,
                "Not Found",
                "User with id 60 not found.",
                "/api/test"
        );
    }


    // =========================================================
    // USER NOT FOUND - EMAIL
    // =========================================================

    @Test
    void shouldHandleUserNotFoundExceptionByEmail() {

        UserNotFoundException exception =
                new UserNotFoundException("test@example.com");

        ResponseEntity<ErrorResponse> response =
                handler.handleUserNotFoundException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                404,
                "Not Found",
                "User with email test@example.com not found.",
                "/api/test"
        );
    }


    // =========================================================
    // REVIEW NOT FOUND
    // =========================================================

    @Test
    void shouldHandleReviewNotFoundException() {

        ReviewNotFoundException exception =
                new ReviewNotFoundException(70L);

        ResponseEntity<ErrorResponse> response =
                handler.handleReviewNotFoundException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                404,
                "Not Found",
                "Review with id 70 not found",
                "/api/test"
        );
    }


    // =========================================================
    // PAYMENT NOT FOUND
    // =========================================================

    @Test
    void shouldHandlePaymentNotFoundException() {

        PaymentNotFoundException exception =
                new PaymentNotFoundException(80L);

        ResponseEntity<ErrorResponse> response =
                handler.handlePaymentNotFoundException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                404,
                "Not Found",
                "Payment not found with id: 80",
                "/api/test"
        );
    }


    // =========================================================
    // NOTIFICATION NOT FOUND
    // =========================================================

    @Test
    void shouldHandleNotificationNotFoundException() {

        NotificationNotFoundException exception =
                new NotificationNotFoundException(90L);

        ResponseEntity<ErrorResponse> response =
                handler.handleNotificationNotFoundException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                404,
                "Not Found",
                "Notification not found with id: 90",
                "/api/test"
        );
    }


    // =========================================================
    // PAYMENT STATE TRANSITION
    // =========================================================

    @Test
    void shouldHandlePaymentStateTransitionException() {

        PaymentStateTransitionException exception =
                new PaymentStateTransitionException(
                        100L,
                        "PENDING",
                        "REFUNDED"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handlePaymentStateTransitionException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                409,
                "Conflict",
                "Payment with id 100 cannot change from PENDING to REFUNDED",
                "/api/test"
        );
    }


    // =========================================================
    // REVIEW NOT ALLOWED
    // =========================================================

    @Test
    void shouldHandleReviewNotAllowedException() {

        ReviewNotAllowedException exception =
                new ReviewNotAllowedException(110L);

        ResponseEntity<ErrorResponse> response =
                handler.handleReviewNotAllowedException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                403,
                "Forbidden",
                "You can only review an event after completing a confirmed booking. Event id: 110",
                "/api/test"
        );
    }


    // =========================================================
    // INVALID CREDENTIALS
    // =========================================================

    @Test
    void shouldHandleInvalidCredentialsException() {

        InvalidCredentialsException exception =
                new InvalidCredentialsException();

        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidCredentialsException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                401,
                "Unauthorized",
                "Invalid email or password",
                "/api/test"
        );
    }


    // =========================================================
    // GENERIC EXCEPTION
    // =========================================================

    @Test
    void shouldHandleGenericException() {

        Exception exception =
                new Exception("Something went wrong");

        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(
                        exception,
                        request
                );

        assertErrorResponse(
                response,
                500,
                "Internal Server Error",
                "Something went wrong",
                "/api/test"
        );
    }


    // =========================================================
    // COMMON ASSERTION
    // =========================================================

    private void assertErrorResponse(
            ResponseEntity<ErrorResponse> response,
            int expectedStatus,
            String expectedError,
            String expectedMessage,
            String expectedPath) {

        assertNotNull(response);

        assertEquals(
                expectedStatus,
                response.getStatusCode().value()
        );

        ErrorResponse body = response.getBody();

        assertNotNull(body);

        assertEquals(
                expectedStatus,
                body.getStatus()
        );

        assertEquals(
                expectedError,
                body.getError()
        );

        assertEquals(
                expectedMessage,
                body.getMessage()
        );

        assertEquals(
                expectedPath,
                body.getPath()
        );

        assertNotNull(body.getTimestamp());
    }
}