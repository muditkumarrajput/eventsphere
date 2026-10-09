package com.eventsphere.eventsphere_backend.integration;

import com.eventsphere.eventsphere_backend.auth.dto.AuthResponse;
import com.eventsphere.eventsphere_backend.auth.dto.LoginOtpResponse;
import com.eventsphere.eventsphere_backend.auth.dto.LoginRequest;
import com.eventsphere.eventsphere_backend.auth.dto.PendingRegistrationResponse;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterResponse;
import com.eventsphere.eventsphere_backend.auth.dto.VerifyOtpRequest;
import com.eventsphere.eventsphere_backend.auth.service.EmailService;
import com.eventsphere.eventsphere_backend.auth.service.SmsService;
import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.dto.CreateBookingRequest;
import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.payment.dto.CreatePaymentRequest;
import com.eventsphere.eventsphere_backend.payment.dto.PaymentResponse;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.profiles.active=dev"
)
class EventSphereE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private SmsService smsService;

    @MockitoBean
    private EmailService emailService;


    @Test
    void registerAndLogin_shouldAuthenticateUserSuccessfully() {

        String email =
                "e2e-" + UUID.randomUUID() + "@example.com";

        String password = "Password@123";

        RegisterResponse registerResponse =
                registerUser(
                        "E2E User",
                        email,
                        password
                );

        assertNotNull(registerResponse);

        String token =
                loginUser(
                        email,
                        password
                );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }


    @Test
    void organizer_shouldCreateEventSuccessfully() {

        String email =
                "organizer-" + UUID.randomUUID() + "@example.com";

        String password = "Password@123";

        registerUser(
                "Organizer User",
                email,
                password
        );

        promoteToOrganizer(email);

        String token =
                loginUser(
                        email,
                        password
                );

        CreateEventRequest request =
                CreateEventRequest.builder()
                        .title("E2E Technology Event")
                        .description("Event created through E2E test")
                        .category(EventCategory.WORKSHOP)
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now()
                                        .plusDays(10)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .build();

        ResponseEntity<EventResponse> response =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                request,
                                authHeaders(token)
                        ),
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());

        assertEquals(
                "E2E Technology Event",
                response.getBody().getTitle()
        );
    }


    @Test
    void user_shouldCreateBookingForOrganizerEventSuccessfully() {

        String organizerEmail =
                "organizer-" + UUID.randomUUID() + "@example.com";

        String organizerPassword =
                "Password@123";

        registerUser(
                "Organizer",
                organizerEmail,
                organizerPassword
        );

        promoteToOrganizer(organizerEmail);

        String organizerToken =
                loginUser(
                        organizerEmail,
                        organizerPassword
                );

        CreateEventRequest eventRequest =
                CreateEventRequest.builder()
                        .title("Booking E2E Event")
                        .description("Booking test event")
                        .category(EventCategory.WORKSHOP)
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now()
                                        .plusDays(10)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .build();

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                eventRequest,
                                authHeaders(organizerToken)
                        ),
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(eventResponse.getBody());

        Long eventId =
                eventResponse.getBody().getId();

        String userEmail =
                "user-" + UUID.randomUUID() + "@example.com";

        String userPassword =
                "Password@123";

        registerUser(
                "Booking User",
                userEmail,
                userPassword
        );

        String userToken =
                loginUser(
                        userEmail,
                        userPassword
                );

        CreateBookingRequest bookingRequest =
                CreateBookingRequest.builder()
                        .eventId(eventId)
                        .numberOfTickets(2)
                        .build();

        ResponseEntity<BookingResponse> bookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                bookingRequest,
                                authHeaders(userToken)
                        ),
                        BookingResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                bookingResponse.getStatusCode()
        );

        assertNotNull(bookingResponse.getBody());

        assertNotNull(
                bookingResponse.getBody().getId()
        );
    }


    @Test
    void user_shouldCompletePaymentAndConfirmBookingSuccessfully() {

        String organizerEmail =
                "payment-organizer-" +
                        UUID.randomUUID() +
                        "@example.com";

        String organizerPassword =
                "Password@123";

        registerUser(
                "Payment Organizer",
                organizerEmail,
                organizerPassword
        );

        promoteToOrganizer(organizerEmail);

        String organizerToken =
                loginUser(
                        organizerEmail,
                        organizerPassword
                );

        CreateEventRequest eventRequest =
                CreateEventRequest.builder()
                        .title("Payment E2E Event")
                        .description("Payment test event")
                        .category(EventCategory.WORKSHOP)
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now()
                                        .plusDays(10)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .build();

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                eventRequest,
                                authHeaders(organizerToken)
                        ),
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(eventResponse.getBody());

        Long eventId =
                eventResponse.getBody().getId();

        String userEmail =
                "payment-user-" +
                        UUID.randomUUID() +
                        "@example.com";

        String userPassword =
                "Password@123";

        registerUser(
                "Payment User",
                userEmail,
                userPassword
        );

        String userToken =
                loginUser(
                        userEmail,
                        userPassword
                );

        CreateBookingRequest bookingRequest =
                CreateBookingRequest.builder()
                        .eventId(eventId)
                        .numberOfTickets(2)
                        .build();

        ResponseEntity<BookingResponse> bookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                bookingRequest,
                                authHeaders(userToken)
                        ),
                        BookingResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                bookingResponse.getStatusCode()
        );

        assertNotNull(bookingResponse.getBody());

        Long bookingId =
                bookingResponse.getBody().getId();

        CreatePaymentRequest paymentRequest =
                new CreatePaymentRequest(bookingId);

        ResponseEntity<PaymentResponse> paymentResponse =
                restTemplate.exchange(
                        "/api/payments",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                paymentRequest,
                                authHeaders(userToken)
                        ),
                        PaymentResponse.class
                );

        assertEquals(
                HttpStatus.CREATED,
                paymentResponse.getStatusCode()
        );

        assertNotNull(paymentResponse.getBody());

        assertNotNull(
                paymentResponse.getBody().getId()
        );
    }


    @Test
    void user_shouldNotBeAbleToCreateEvent() {

        String email =
                "normal-user-" +
                        UUID.randomUUID() +
                        "@example.com";

        String password =
                "Password@123";

        registerUser(
                "Normal User",
                email,
                password
        );

        String token =
                loginUser(
                        email,
                        password
                );

        CreateEventRequest request =
                CreateEventRequest.builder()
                        .title("Unauthorized Event")
                        .description("Should not be created")
                        .category(EventCategory.WORKSHOP)
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now()
                                        .plusDays(10)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .build();

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                request,
                                authHeaders(token)
                        ),
                        String.class
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );
    }


    @Test
    void organizer_shouldNotBeAbleToUpdateAnotherOrganizersEvent() {

        String organizerOneEmail =
                "organizer-one-" +
                        UUID.randomUUID() +
                        "@example.com";

        String organizerPassword =
                "Password@123";

        registerUser(
                "Organizer One",
                organizerOneEmail,
                organizerPassword
        );

        promoteToOrganizer(organizerOneEmail);

        String organizerOneToken =
                loginUser(
                        organizerOneEmail,
                        organizerPassword
                );

        CreateEventRequest createRequest =
                CreateEventRequest.builder()
                        .title("Owner Event")
                        .description("Owner event")
                        .category(EventCategory.WORKSHOP)
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now()
                                        .plusDays(10)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .build();

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                createRequest,
                                authHeaders(organizerOneToken)
                        ),
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(eventResponse.getBody());

        Long eventId =
                eventResponse.getBody().getId();

        String organizerTwoEmail =
                "organizer-two-" +
                        UUID.randomUUID() +
                        "@example.com";

        registerUser(
                "Organizer Two",
                organizerTwoEmail,
                organizerPassword
        );

        promoteToOrganizer(organizerTwoEmail);

        String organizerTwoToken =
                loginUser(
                        organizerTwoEmail,
                        organizerPassword
                );

        UpdateEventRequest updateRequest =
                UpdateEventRequest.builder()
                        .title("Unauthorized Update")
                        .description("Unauthorized update attempt")
                        .category(EventCategory.WORKSHOP)
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now()
                                        .plusDays(15)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .build();

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/api/events/" + eventId,
                        HttpMethod.PUT,
                        new HttpEntity<>(
                                updateRequest,
                                authHeaders(organizerTwoToken)
                        ),
                        String.class
                );

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );
    }


    @Test
    void user_shouldNotBeAbleToAccessAnotherUsersBooking() {

        String organizerEmail =
                "booking-owner-" +
                        UUID.randomUUID() +
                        "@example.com";

        String password =
                "Password@123";

        registerUser(
                "Booking Organizer",
                organizerEmail,
                password
        );

        promoteToOrganizer(organizerEmail);

        String organizerToken =
                loginUser(
                        organizerEmail,
                        password
                );

        CreateEventRequest eventRequest =
                CreateEventRequest.builder()
                        .title("Private Booking Event")
                        .description("Private booking event")
                        .category(EventCategory.WORKSHOP)
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now()
                                        .plusDays(10)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .build();

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                eventRequest,
                                authHeaders(organizerToken)
                        ),
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(eventResponse.getBody());

        Long eventId =
                eventResponse.getBody().getId();

        String firstUserEmail =
                "first-user-" +
                        UUID.randomUUID() +
                        "@example.com";

        registerUser(
                "First User",
                firstUserEmail,
                password
        );

        String firstUserToken =
                loginUser(
                        firstUserEmail,
                        password
                );

        CreateBookingRequest bookingRequest =
                CreateBookingRequest.builder()
                        .eventId(eventId)
                        .numberOfTickets(1)
                        .build();

        ResponseEntity<BookingResponse> bookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                bookingRequest,
                                authHeaders(firstUserToken)
                        ),
                        BookingResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                bookingResponse.getStatusCode()
        );

        assertNotNull(bookingResponse.getBody());

        Long bookingId =
                bookingResponse.getBody().getId();

        String secondUserEmail =
                "second-user-" +
                        UUID.randomUUID() +
                        "@example.com";

        registerUser(
                "Second User",
                secondUserEmail,
                password
        );

        String secondUserToken =
                loginUser(
                        secondUserEmail,
                        password
                );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/api/bookings/" + bookingId,
                        HttpMethod.GET,
                        new HttpEntity<>(
                                authHeaders(secondUserToken)
                        ),
                        String.class
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );
    }


    @Test
    void organizer_shouldBeAbleToCancelEventWithBookings() {

        String organizerEmail =
                "cancel-organizer-" +
                        UUID.randomUUID() +
                        "@example.com";

        String password =
                "Password@123";

        registerUser(
                "Cancel Organizer",
                organizerEmail,
                password
        );

        promoteToOrganizer(organizerEmail);

        String organizerToken =
                loginUser(
                        organizerEmail,
                        password
                );

        CreateEventRequest eventRequest =
                CreateEventRequest.builder()
                        .title("Cancellation Event")
                        .description("Cancellation test")
                        .category(EventCategory.WORKSHOP)
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now()
                                        .plusDays(10)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .build();

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                eventRequest,
                                authHeaders(organizerToken)
                        ),
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(eventResponse.getBody());

        Long eventId =
                eventResponse.getBody().getId();

        String userEmail =
                "cancel-user-" +
                        UUID.randomUUID() +
                        "@example.com";

        registerUser(
                "Cancel User",
                userEmail,
                password
        );

        String userToken =
                loginUser(
                        userEmail,
                        password
                );

        CreateBookingRequest bookingRequest =
                CreateBookingRequest.builder()
                        .eventId(eventId)
                        .numberOfTickets(1)
                        .build();

        ResponseEntity<BookingResponse> bookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                bookingRequest,
                                authHeaders(userToken)
                        ),
                        BookingResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                bookingResponse.getStatusCode()
        );

        assertNotNull(bookingResponse.getBody());

        ResponseEntity<EventResponse> cancelResponse =
                restTemplate.exchange(
                        "/api/events/" + eventId + "/cancel",
                        HttpMethod.PATCH,
                        new HttpEntity<>(
                                authHeaders(organizerToken)
                        ),
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                cancelResponse.getStatusCode()
        );

        assertNotNull(cancelResponse.getBody());
    }


    @Test
    void user_shouldNotBeAbleToBookBeyondEventCapacity() {

        String organizerEmail =
                "capacity-organizer-" +
                        UUID.randomUUID() +
                        "@example.com";

        String password =
                "Password@123";

        registerUser(
                "Capacity Organizer",
                organizerEmail,
                password
        );

        promoteToOrganizer(organizerEmail);

        String organizerToken =
                loginUser(
                        organizerEmail,
                        password
                );

        CreateEventRequest eventRequest =
                CreateEventRequest.builder()
                        .title("Capacity Event")
                        .description("Capacity test")
                        .category(EventCategory.WORKSHOP)
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now()
                                        .plusDays(10)
                        )
                        .capacity(2)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .build();

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                eventRequest,
                                authHeaders(organizerToken)
                        ),
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(eventResponse.getBody());

        Long eventId =
                eventResponse.getBody().getId();

        String userEmail =
                "capacity-user-" +
                        UUID.randomUUID() +
                        "@example.com";

        registerUser(
                "Capacity User",
                userEmail,
                password
        );

        String userToken =
                loginUser(
                        userEmail,
                        password
                );

        CreateBookingRequest bookingRequest =
                CreateBookingRequest.builder()
                        .eventId(eventId)
                        .numberOfTickets(3)
                        .build();

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        new HttpEntity<>(
                                bookingRequest,
                                authHeaders(userToken)
                        ),
                        String.class
                );

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );
    }



    private RegisterResponse registerUser(
            String name,
            String email,
            String password
    ) {

        String phoneNumber = uniquePhoneNumber();

        final String[] emailOtp = new String[1];

        org.mockito.Mockito.doAnswer(
                invocation -> {
                    emailOtp[0] = invocation.getArgument(1);
                    return null;
                }
        ).when(emailService).sendOtp(
                org.mockito.Mockito.eq(email),
                org.mockito.Mockito.anyString(),
                org.mockito.Mockito.eq("Registration")
        );

        RegisterRequest registerRequest =
                RegisterRequest.builder()
                        .name(name)
                        .email(email)
                        .password(password)
                        .confirmPassword(password)
                        .phoneNumber(phoneNumber)
                        .build();

        ResponseEntity<PendingRegistrationResponse> registerResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        registerRequest,
                        PendingRegistrationResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                registerResponse.getStatusCode()
        );

        assertNotNull(registerResponse.getBody());

        assertFalse(
                registerResponse.getBody().isMobileOtpSent()
        );

        assertTrue(
                registerResponse.getBody().isEmailOtpSent()
        );

        assertNotNull(emailOtp[0]);

        ResponseEntity<RegisterResponse> emailVerificationResponse =
                restTemplate.postForEntity(
                        "/api/auth/register/verify-email",
                        VerifyOtpRequest.builder()
                                .target(email)
                                .otp(emailOtp[0])
                                .build(),
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                emailVerificationResponse.getStatusCode()
        );

        assertNotNull(emailVerificationResponse.getBody());

        return emailVerificationResponse.getBody();
    }


    private String loginUser(
            String email,
            String password
    ) {
        ResponseEntity<AuthResponse> loginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        LoginRequest.builder()
                                .identifier(email)
                                .password(password)
                                .build(),
                        AuthResponse.class
                );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());

        String token = loginResponse.getBody().getToken();

        assertNotNull(token);
        assertFalse(token.isBlank());

        return token;
    }


    private String uniquePhoneNumber() {

        String digits =
                UUID.randomUUID()
                        .toString()
                        .replaceAll("\\D", "");

        while (digits.length() < 9) {
            digits +=
                    UUID.randomUUID()
                            .toString()
                            .replaceAll("\\D", "");
        }

        return "9" +
                digits.substring(0, 9);
    }


    private void promoteToOrganizer(
            String email
    ) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "User was not found: " +
                                                email
                                )
                        );

        user.setRole(Role.ORGANIZER);

        userRepository.save(user);
    }


    private HttpHeaders authHeaders(
            String token
    ) {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setBearerAuth(token);

        headers.setContentType(
                org.springframework.http.MediaType.APPLICATION_JSON
        );

        return headers;
    }
}
