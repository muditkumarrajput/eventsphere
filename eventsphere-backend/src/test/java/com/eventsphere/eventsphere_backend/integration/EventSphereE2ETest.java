package com.eventsphere.eventsphere_backend.integration;

import com.eventsphere.eventsphere_backend.auth.dto.AuthResponse;
import com.eventsphere.eventsphere_backend.auth.dto.LoginRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterRequest;
import com.eventsphere.eventsphere_backend.auth.dto.RegisterResponse;
import com.eventsphere.eventsphere_backend.event.dto.CreateEventRequest;
import com.eventsphere.eventsphere_backend.event.dto.EventResponse;
import com.eventsphere.eventsphere_backend.event.dto.UpdateEventRequest;
import com.eventsphere.eventsphere_backend.event.entity.EventCategory;
import com.eventsphere.eventsphere_backend.user.entity.Role;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.dto.CreateBookingRequest;
import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.dto.CreateBookingRequest;
import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import com.eventsphere.eventsphere_backend.notification.dto.NotificationResponse;
import com.eventsphere.eventsphere_backend.payment.dto.CreatePaymentRequest;
import com.eventsphere.eventsphere_backend.payment.dto.PaymentResponse;
import com.eventsphere.eventsphere_backend.payment.entity.PaymentStatus;

import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class EventSphereE2ETest extends AbstractPostgresIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    // =========================================================
    // STEP 1
    // REGISTER + LOGIN + JWT AUTHENTICATION
    // =========================================================

    @Test
    void registerAndLogin_shouldAuthenticateUserSuccessfully() {

        // =========================================================
        // 1. REGISTER USER
        // =========================================================

        String email =
                "e2e-" + UUID.randomUUID() + "@test.com";

        RegisterRequest registerRequest =
                RegisterRequest.builder()
                        .name("E2E Test User")
                        .email(email)
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        ResponseEntity<RegisterResponse> registerResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        registerRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                registerResponse.getStatusCode()
        );

        assertNotNull(registerResponse.getBody());

        assertNotNull(
                registerResponse.getBody().getId()
        );

        assertEquals(
                email,
                registerResponse.getBody().getEmail()
        );

        assertEquals(
                "E2E Test User",
                registerResponse.getBody().getName()
        );

        assertEquals(
                "9876543210",
                registerResponse.getBody().getPhoneNumber()
        );

        assertNotNull(
                registerResponse.getBody().getRole()
        );

        // Registration should create a USER
        assertEquals(
                Role.USER,
                registerResponse.getBody().getRole()
        );

        // =========================================================
        // 2. LOGIN USER
        // =========================================================

        LoginRequest loginRequest =
                LoginRequest.builder()
                        .email(email)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> loginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        loginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                loginResponse.getStatusCode()
        );

        assertNotNull(loginResponse.getBody());

        // =========================================================
        // 3. VERIFY JWT
        // =========================================================

        String token =
                loginResponse.getBody().getToken();

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                3,
                token.split("\\.").length
        );

        // =========================================================
        // 4. USE JWT ON PROTECTED ENDPOINT
        // =========================================================

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(token);

        HttpEntity<Void> authenticatedRequest =
                new HttpEntity<>(headers);

        ResponseEntity<String> protectedResponse =
                restTemplate.exchange(
                        "/api/users/me",
                        HttpMethod.GET,
                        authenticatedRequest,
                        String.class
                );

        assertNotEquals(
                HttpStatus.UNAUTHORIZED,
                protectedResponse.getStatusCode()
        );
    }

    // =========================================================
    // STEP 2
    // REGISTER + PROMOTE TO ORGANIZER + LOGIN + CREATE EVENT
    // =========================================================

    @Test
    void organizer_shouldCreateEventSuccessfully() {

        // =========================================================
        // 1. REGISTER USER
        // =========================================================

        String email =
                "organizer-" + UUID.randomUUID() + "@test.com";

        RegisterRequest registerRequest =
                RegisterRequest.builder()
                        .name("E2E Organizer")
                        .email(email)
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        ResponseEntity<RegisterResponse> registerResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        registerRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                registerResponse.getStatusCode()
        );

        assertNotNull(registerResponse.getBody());

        assertEquals(
                Role.USER,
                registerResponse.getBody().getRole()
        );

        // =========================================================
        // 2. PROMOTE TEST USER TO ORGANIZER
        // =========================================================

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "Registered E2E user was not found"
                                )
                        );

        user.setRole(Role.ORGANIZER);

        userRepository.save(user);

        // Verify database state
        User organizer =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "Organizer was not found after role update"
                                )
                        );

        assertEquals(
                Role.ORGANIZER,
                organizer.getRole()
        );

        // =========================================================
        // 3. LOGIN AS ORGANIZER
        // =========================================================

        LoginRequest loginRequest =
                LoginRequest.builder()
                        .email(email)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> loginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        loginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                loginResponse.getStatusCode()
        );

        assertNotNull(loginResponse.getBody());

        String token =
                loginResponse.getBody().getToken();

        assertNotNull(token);
        assertFalse(token.isBlank());

        // =========================================================
        // 4. CREATE EVENT
        // =========================================================

        CreateEventRequest createEventRequest =
                CreateEventRequest.builder()
                        .title("E2E Test Concert")
                        .description(
                                "Concert created during E2E testing"
                        )
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .category(EventCategory.CONCERT)
                        .build();

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(token);
        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateEventRequest> eventRequest =
                new HttpEntity<>(
                        createEventRequest,
                        headers
                );

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        eventRequest,
                        EventResponse.class
                );

        // =========================================================
        // 5. VERIFY EVENT CREATION
        // =========================================================

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(eventResponse.getBody());

        EventResponse createdEvent =
                eventResponse.getBody();

        assertNotNull(createdEvent.getId());

        assertEquals(
                "E2E Test Concert",
                createdEvent.getTitle()
        );

        assertEquals(
                "Concert created during E2E testing",
                createdEvent.getDescription()
        );

        assertEquals(
                "Lucknow",
                createdEvent.getLocation()
        );

        assertEquals(
                100,
                createdEvent.getCapacity()
        );

        assertEquals(
                new BigDecimal("500.00"),
                createdEvent.getTicketPrice()
        );

        assertEquals(
                EventCategory.CONCERT,
                createdEvent.getCategory()
        );

        assertTrue(
                createdEvent.getEventDate()
                        .isAfter(LocalDateTime.now())
        );
    }
    // =========================================================
// STEP 3
// ORGANIZER CREATES EVENT + USER CREATES BOOKING
// =========================================================

    @Test
    void user_shouldCreateBookingForOrganizerEventSuccessfully() {

        // =========================================================
        // 1. REGISTER ORGANIZER
        // =========================================================

        String organizerEmail =
                "organizer-" + UUID.randomUUID() + "@test.com";

        RegisterRequest organizerRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Organizer")
                        .email(organizerEmail)
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        ResponseEntity<RegisterResponse> organizerRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        organizerRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerRegisterResponse.getStatusCode()
        );

        assertNotNull(organizerRegisterResponse.getBody());

        // =========================================================
        // 2. PROMOTE ORGANIZER
        // =========================================================

        User organizer =
                userRepository.findByEmail(organizerEmail)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "Organizer was not found"
                                )
                        );

        organizer.setRole(Role.ORGANIZER);

        userRepository.save(organizer);

        // =========================================================
        // 3. LOGIN ORGANIZER
        // =========================================================

        LoginRequest organizerLoginRequest =
                LoginRequest.builder()
                        .email(organizerEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> organizerLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        organizerLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerLoginResponse.getStatusCode()
        );

        assertNotNull(organizerLoginResponse.getBody());

        String organizerToken =
                organizerLoginResponse.getBody().getToken();

        assertNotNull(organizerToken);
        assertFalse(organizerToken.isBlank());

        // =========================================================
        // 4. ORGANIZER CREATES EVENT
        // =========================================================

        CreateEventRequest createEventRequest =
                CreateEventRequest.builder()
                        .title("E2E Booking Test Event")
                        .description(
                                "Event created for booking E2E testing"
                        )
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .category(EventCategory.CONCERT)
                        .build();

        HttpHeaders organizerHeaders =
                new HttpHeaders();

        organizerHeaders.setBearerAuth(organizerToken);

        organizerHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateEventRequest> eventRequest =
                new HttpEntity<>(
                        createEventRequest,
                        organizerHeaders
                );

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        eventRequest,
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(eventResponse.getBody());

        EventResponse createdEvent =
                eventResponse.getBody();

        assertNotNull(createdEvent.getId());

        assertEquals(
                "E2E Booking Test Event",
                createdEvent.getTitle()
        );

        assertEquals(
                100,
                createdEvent.getCapacity()
        );

        assertEquals(
                new BigDecimal("500.00"),
                createdEvent.getTicketPrice()
        );

        // =========================================================
        // 5. REGISTER NORMAL USER
        // =========================================================

        String userEmail =
                "user-" + UUID.randomUUID() + "@test.com";

        RegisterRequest userRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Booking User")
                        .email(userEmail)
                        .password("password123")
                        .phoneNumber("9123456789")
                        .build();

        ResponseEntity<RegisterResponse> userRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        userRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userRegisterResponse.getStatusCode()
        );

        assertNotNull(userRegisterResponse.getBody());

        assertEquals(
                Role.USER,
                userRegisterResponse.getBody().getRole()
        );

        // =========================================================
        // 6. LOGIN USER
        // =========================================================

        LoginRequest userLoginRequest =
                LoginRequest.builder()
                        .email(userEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> userLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        userLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userLoginResponse.getStatusCode()
        );

        assertNotNull(userLoginResponse.getBody());

        String userToken =
                userLoginResponse.getBody().getToken();

        assertNotNull(userToken);
        assertFalse(userToken.isBlank());

        // =========================================================
        // 7. USER CREATES BOOKING
        // =========================================================

        CreateBookingRequest createBookingRequest =
                CreateBookingRequest.builder()
                        .eventId(createdEvent.getId())
                        .numberOfTickets(2)
                        .build();

        HttpHeaders userHeaders =
                new HttpHeaders();

        userHeaders.setBearerAuth(userToken);

        userHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateBookingRequest> bookingRequest =
                new HttpEntity<>(
                        createBookingRequest,
                        userHeaders
                );

        ResponseEntity<BookingResponse> bookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        bookingRequest,
                        BookingResponse.class
                );

        // =========================================================
        // 8. VERIFY BOOKING
        // =========================================================

        assertEquals(
                HttpStatus.OK,
                bookingResponse.getStatusCode()
        );

        assertNotNull(bookingResponse.getBody());

        BookingResponse createdBooking =
                bookingResponse.getBody();

        assertNotNull(createdBooking.getId());

        assertNotNull(
                createdBooking.getBookingReference()
        );

        assertEquals(
                createdEvent.getId(),
                createdBooking.getEventId()
        );

        assertEquals(
                2,
                createdBooking.getNumberOfTickets()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                createdBooking.getTotalAmount()
        );

        assertEquals(
                BookingStatus.PENDING,
                createdBooking.getBookingStatus()
        );

        assertNotNull(
                createdBooking.getBookingDate()
        );

        assertNotNull(
                createdBooking.getCreatedAt()
        );
    }
    // =========================================================
// STEP 4
// BOOKING → PAYMENT → SUCCESS → CONFIRMED → NOTIFICATION
// =========================================================

    @Test
    void user_shouldCompletePaymentAndConfirmBookingSuccessfully() {

        // =========================================================
        // 1. REGISTER ORGANIZER
        // =========================================================

        String organizerEmail =
                "organizer-" + UUID.randomUUID() + "@test.com";

        RegisterRequest organizerRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Payment Organizer")
                        .email(organizerEmail)
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        ResponseEntity<RegisterResponse> organizerRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        organizerRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerRegisterResponse.getStatusCode()
        );

        assertNotNull(
                organizerRegisterResponse.getBody()
        );

        // =========================================================
        // 2. PROMOTE USER TO ORGANIZER
        // =========================================================

        User organizer =
                userRepository.findByEmail(organizerEmail)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "Organizer was not found"
                                )
                        );

        organizer.setRole(Role.ORGANIZER);

        userRepository.save(organizer);

        // =========================================================
        // 3. LOGIN ORGANIZER
        // =========================================================

        LoginRequest organizerLoginRequest =
                LoginRequest.builder()
                        .email(organizerEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> organizerLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        organizerLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerLoginResponse.getStatusCode()
        );

        assertNotNull(
                organizerLoginResponse.getBody()
        );

        String organizerToken =
                organizerLoginResponse.getBody().getToken();

        assertNotNull(organizerToken);
        assertFalse(organizerToken.isBlank());

        // =========================================================
        // 4. CREATE EVENT
        // =========================================================

        CreateEventRequest createEventRequest =
                CreateEventRequest.builder()
                        .title("E2E Payment Test Event")
                        .description(
                                "Event created for payment E2E testing"
                        )
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .category(EventCategory.CONCERT)
                        .build();

        HttpHeaders organizerHeaders =
                new HttpHeaders();

        organizerHeaders.setBearerAuth(organizerToken);

        organizerHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateEventRequest> eventRequest =
                new HttpEntity<>(
                        createEventRequest,
                        organizerHeaders
                );

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        eventRequest,
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(eventResponse.getBody());

        EventResponse createdEvent =
                eventResponse.getBody();

        assertNotNull(createdEvent.getId());

        assertEquals(
                new BigDecimal("500.00"),
                createdEvent.getTicketPrice()
        );

        // =========================================================
        // 5. REGISTER NORMAL USER
        // =========================================================

        String userEmail =
                "user-" + UUID.randomUUID() + "@test.com";

        RegisterRequest userRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Payment User")
                        .email(userEmail)
                        .password("password123")
                        .phoneNumber("9123456789")
                        .build();

        ResponseEntity<RegisterResponse> userRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        userRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userRegisterResponse.getStatusCode()
        );

        assertNotNull(
                userRegisterResponse.getBody()
        );

        Long userId =
                userRegisterResponse.getBody().getId();

        assertNotNull(userId);

        assertEquals(
                Role.USER,
                userRegisterResponse.getBody().getRole()
        );

        // =========================================================
        // 6. LOGIN USER
        // =========================================================

        LoginRequest userLoginRequest =
                LoginRequest.builder()
                        .email(userEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> userLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        userLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userLoginResponse.getStatusCode()
        );

        assertNotNull(
                userLoginResponse.getBody()
        );

        String userToken =
                userLoginResponse.getBody().getToken();

        assertNotNull(userToken);
        assertFalse(userToken.isBlank());

        // =========================================================
        // 7. CREATE BOOKING
        // =========================================================

        CreateBookingRequest createBookingRequest =
                CreateBookingRequest.builder()
                        .eventId(createdEvent.getId())
                        .numberOfTickets(2)
                        .build();

        HttpHeaders userHeaders =
                new HttpHeaders();

        userHeaders.setBearerAuth(userToken);

        userHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateBookingRequest> bookingRequest =
                new HttpEntity<>(
                        createBookingRequest,
                        userHeaders
                );

        ResponseEntity<BookingResponse> bookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        bookingRequest,
                        BookingResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                bookingResponse.getStatusCode()
        );

        assertNotNull(
                bookingResponse.getBody()
        );

        BookingResponse createdBooking =
                bookingResponse.getBody();

        assertNotNull(createdBooking.getId());

        assertEquals(
                userId,
                createdBooking.getUserId()
        );

        assertEquals(
                createdEvent.getId(),
                createdBooking.getEventId()
        );

        assertEquals(
                2,
                createdBooking.getNumberOfTickets()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                createdBooking.getTotalAmount()
        );

        assertEquals(
                BookingStatus.PENDING,
                createdBooking.getBookingStatus()
        );

        // =========================================================
        // 8. CREATE PAYMENT
        // =========================================================

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(
                        createdBooking.getId()
                );

        HttpEntity<CreatePaymentRequest> paymentRequest =
                new HttpEntity<>(
                        createPaymentRequest,
                        userHeaders
                );

        ResponseEntity<PaymentResponse> paymentResponse =
                restTemplate.exchange(
                        "/api/payments",
                        HttpMethod.POST,
                        paymentRequest,
                        PaymentResponse.class
                );

        // PaymentController explicitly returns 201
        assertEquals(
                HttpStatus.CREATED,
                paymentResponse.getStatusCode()
        );

        assertNotNull(
                paymentResponse.getBody()
        );

        PaymentResponse createdPayment =
                paymentResponse.getBody();

        assertNotNull(createdPayment.getId());

        assertNotNull(
                createdPayment.getPaymentReference()
        );

        assertEquals(
                createdBooking.getId(),
                createdPayment.getBookingId()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                createdPayment.getAmount()
        );

        assertEquals(
                PaymentStatus.PENDING,
                createdPayment.getPaymentStatus()
        );

        assertNotNull(
                createdPayment.getPaymentDate()
        );

        // =========================================================
        // 9. MARK PAYMENT AS SUCCESSFUL
        // =========================================================

        ResponseEntity<PaymentResponse> successResponse =
                restTemplate.exchange(
                        "/api/payments/"
                                + createdPayment.getId()
                                + "/success",
                        HttpMethod.PATCH,
                        new HttpEntity<>(userHeaders),
                        PaymentResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                successResponse.getStatusCode()
        );

        assertNotNull(
                successResponse.getBody()
        );

        PaymentResponse successfulPayment =
                successResponse.getBody();

        assertEquals(
                createdPayment.getId(),
                successfulPayment.getId()
        );

        assertEquals(
                PaymentStatus.SUCCESS,
                successfulPayment.getPaymentStatus()
        );

        assertEquals(
                createdBooking.getId(),
                successfulPayment.getBookingId()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                successfulPayment.getAmount()
        );

        // =========================================================
        // 10. VERIFY BOOKING IS NOW CONFIRMED
        // =========================================================

        ResponseEntity<BookingResponse> confirmedBookingResponse =
                restTemplate.exchange(
                        "/api/bookings/"
                                + createdBooking.getId(),
                        HttpMethod.GET,
                        new HttpEntity<>(userHeaders),
                        BookingResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                confirmedBookingResponse.getStatusCode()
        );

        assertNotNull(
                confirmedBookingResponse.getBody()
        );

        BookingResponse confirmedBooking =
                confirmedBookingResponse.getBody();

        assertEquals(
                createdBooking.getId(),
                confirmedBooking.getId()
        );

        assertEquals(
                BookingStatus.CONFIRMED,
                confirmedBooking.getBookingStatus()
        );

        assertEquals(
                createdBooking.getUserId(),
                confirmedBooking.getUserId()
        );

        assertEquals(
                createdBooking.getEventId(),
                confirmedBooking.getEventId()
        );

        assertEquals(
                2,
                confirmedBooking.getNumberOfTickets()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                confirmedBooking.getTotalAmount()
        );

        // =========================================================
        // 11. VERIFY NOTIFICATION WAS CREATED
        // =========================================================

        ResponseEntity<NotificationResponse[]> notificationResponse =
                restTemplate.exchange(
                        "/api/notifications",
                        HttpMethod.GET,
                        new HttpEntity<>(userHeaders),
                        NotificationResponse[].class
                );

        assertEquals(
                HttpStatus.OK,
                notificationResponse.getStatusCode()
        );

        assertNotNull(
                notificationResponse.getBody()
        );

        List<NotificationResponse> notifications =
                List.of(notificationResponse.getBody());

        assertFalse(
                notifications.isEmpty()
        );
    }
    // =========================================================
// STEP 5
// USER CANNOT CREATE EVENT
// =========================================================

    @Test
    void user_shouldNotBeAbleToCreateEvent() {

        // =========================================================
        // 1. REGISTER NORMAL USER
        // =========================================================

        String userEmail =
                "user-" + UUID.randomUUID() + "@test.com";

        RegisterRequest registerRequest =
                RegisterRequest.builder()
                        .name("E2E Unauthorized User")
                        .email(userEmail)
                        .password("password123")
                        .phoneNumber("9123456789")
                        .build();

        ResponseEntity<RegisterResponse> registerResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        registerRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                registerResponse.getStatusCode()
        );

        assertNotNull(
                registerResponse.getBody()
        );

        assertEquals(
                Role.USER,
                registerResponse.getBody().getRole()
        );

        // =========================================================
        // 2. LOGIN USER
        // =========================================================

        LoginRequest loginRequest =
                LoginRequest.builder()
                        .email(userEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> loginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        loginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                loginResponse.getStatusCode()
        );

        assertNotNull(
                loginResponse.getBody()
        );

        String userToken =
                loginResponse.getBody().getToken();

        assertNotNull(userToken);
        assertFalse(userToken.isBlank());

        // =========================================================
        // 3. USER ATTEMPTS TO CREATE EVENT
        // =========================================================

        CreateEventRequest createEventRequest =
                CreateEventRequest.builder()
                        .title("Unauthorized Event")
                        .description(
                                "This event should not be created"
                        )
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .category(EventCategory.CONCERT)
                        .build();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setBearerAuth(userToken);

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateEventRequest> request =
                new HttpEntity<>(
                        createEventRequest,
                        headers
                );

        ResponseEntity<String> response =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        request,
                        String.class
                );

        // =========================================================
        // 4. VERIFY ACCESS IS DENIED
        // =========================================================

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );
    }
    // =========================================================
// STEP 6
// ORGANIZER CANNOT UPDATE ANOTHER ORGANIZER'S EVENT
// =========================================================

    @Test
    void organizer_shouldNotBeAbleToUpdateAnotherOrganizersEvent() {

        // =========================================================
        // 1. REGISTER ORGANIZER A
        // =========================================================

        String organizerAEmail =
                "organizer-a-" + UUID.randomUUID() + "@test.com";

        RegisterRequest organizerARegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Organizer A")
                        .email(organizerAEmail)
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        ResponseEntity<RegisterResponse> organizerARegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        organizerARegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerARegisterResponse.getStatusCode()
        );

        assertNotNull(
                organizerARegisterResponse.getBody()
        );

        // =========================================================
        // 2. PROMOTE ORGANIZER A
        // =========================================================

        User organizerA =
                userRepository.findByEmail(organizerAEmail)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "Organizer A was not found"
                                )
                        );

        organizerA.setRole(Role.ORGANIZER);

        userRepository.save(organizerA);

        // =========================================================
        // 3. LOGIN ORGANIZER A
        // =========================================================

        LoginRequest organizerALoginRequest =
                LoginRequest.builder()
                        .email(organizerAEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> organizerALoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        organizerALoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerALoginResponse.getStatusCode()
        );

        assertNotNull(
                organizerALoginResponse.getBody()
        );

        String organizerAToken =
                organizerALoginResponse.getBody().getToken();

        assertNotNull(organizerAToken);
        assertFalse(organizerAToken.isBlank());

        // =========================================================
        // 4. ORGANIZER A CREATES EVENT
        // =========================================================

        CreateEventRequest createEventRequest =
                CreateEventRequest.builder()
                        .title("Organizer A Event")
                        .description(
                                "Event owned by Organizer A"
                        )
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .category(EventCategory.CONCERT)
                        .build();

        HttpHeaders organizerAHeaders =
                new HttpHeaders();

        organizerAHeaders.setBearerAuth(
                organizerAToken
        );

        organizerAHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateEventRequest> createEventHttpRequest =
                new HttpEntity<>(
                        createEventRequest,
                        organizerAHeaders
                );

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        createEventHttpRequest,
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(
                eventResponse.getBody()
        );

        EventResponse organizerAEvent =
                eventResponse.getBody();

        assertNotNull(
                organizerAEvent.getId()
        );

        assertEquals(
                "Organizer A Event",
                organizerAEvent.getTitle()
        );

        // =========================================================
        // 5. REGISTER ORGANIZER B
        // =========================================================

        String organizerBEmail =
                "organizer-b-" + UUID.randomUUID() + "@test.com";

        RegisterRequest organizerBRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Organizer B")
                        .email(organizerBEmail)
                        .password("password123")
                        .phoneNumber("9123456789")
                        .build();

        ResponseEntity<RegisterResponse> organizerBRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        organizerBRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerBRegisterResponse.getStatusCode()
        );

        assertNotNull(
                organizerBRegisterResponse.getBody()
        );

        // =========================================================
        // 6. PROMOTE ORGANIZER B
        // =========================================================

        User organizerB =
                userRepository.findByEmail(organizerBEmail)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "Organizer B was not found"
                                )
                        );

        organizerB.setRole(Role.ORGANIZER);

        userRepository.save(organizerB);

        // =========================================================
        // 7. LOGIN ORGANIZER B
        // =========================================================

        LoginRequest organizerBLoginRequest =
                LoginRequest.builder()
                        .email(organizerBEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> organizerBLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        organizerBLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerBLoginResponse.getStatusCode()
        );

        assertNotNull(
                organizerBLoginResponse.getBody()
        );

        String organizerBToken =
                organizerBLoginResponse.getBody().getToken();

        assertNotNull(organizerBToken);
        assertFalse(organizerBToken.isBlank());

        // =========================================================
        // 8. ORGANIZER B ATTEMPTS TO UPDATE ORGANIZER A'S EVENT
        // =========================================================

        UpdateEventRequest updateEventRequest =
                UpdateEventRequest.builder()
                        .title("Unauthorized Updated Event")
                        .description(
                                "Organizer B should not be able to update this"
                        )
                        .location("Kanpur")
                        .eventDate(
                                LocalDateTime.now().plusDays(45)
                        )
                        .capacity(200)
                        .ticketPrice(
                                new BigDecimal("1000.00")
                        )
                        .category(EventCategory.FESTIVAL)
                        .build();

        HttpHeaders organizerBHeaders =
                new HttpHeaders();

        organizerBHeaders.setBearerAuth(
                organizerBToken
        );

        organizerBHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<UpdateEventRequest> updateRequest =
                new HttpEntity<>(
                        updateEventRequest,
                        organizerBHeaders
                );

        ResponseEntity<String> updateResponse =
                restTemplate.exchange(
                        "/api/events/"
                                + organizerAEvent.getId(),
                        HttpMethod.PUT,
                        updateRequest,
                        String.class
                );

        // =========================================================
        // 9. VERIFY ACCESS IS DENIED
        // =========================================================

        assertEquals(
                HttpStatus.FORBIDDEN,
                updateResponse.getStatusCode()
        );
    }
    // =========================================================
// STEP 7
// USER CANNOT ACCESS ANOTHER USER'S BOOKING
// =========================================================

    @Test
    void user_shouldNotBeAbleToAccessAnotherUsersBooking() {

        // =========================================================
        // 1. REGISTER ORGANIZER
        // =========================================================

        String organizerEmail =
                "organizer-" + UUID.randomUUID() + "@test.com";

        RegisterRequest organizerRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Booking Organizer")
                        .email(organizerEmail)
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        ResponseEntity<RegisterResponse> organizerRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        organizerRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerRegisterResponse.getStatusCode()
        );

        assertNotNull(
                organizerRegisterResponse.getBody()
        );

        // =========================================================
        // 2. PROMOTE ORGANIZER
        // =========================================================

        User organizer =
                userRepository.findByEmail(organizerEmail)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "Organizer was not found"
                                )
                        );

        organizer.setRole(Role.ORGANIZER);

        userRepository.save(organizer);

        // =========================================================
        // 3. LOGIN ORGANIZER
        // =========================================================

        LoginRequest organizerLoginRequest =
                LoginRequest.builder()
                        .email(organizerEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> organizerLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        organizerLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerLoginResponse.getStatusCode()
        );

        assertNotNull(
                organizerLoginResponse.getBody()
        );

        String organizerToken =
                organizerLoginResponse.getBody().getToken();

        assertNotNull(organizerToken);
        assertFalse(organizerToken.isBlank());

        // =========================================================
        // 4. ORGANIZER CREATES EVENT
        // =========================================================

        CreateEventRequest createEventRequest =
                CreateEventRequest.builder()
                        .title("E2E Booking Ownership Event")
                        .description(
                                "Event for booking ownership testing"
                        )
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .category(EventCategory.CONCERT)
                        .build();

        HttpHeaders organizerHeaders =
                new HttpHeaders();

        organizerHeaders.setBearerAuth(
                organizerToken
        );

        organizerHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateEventRequest> eventRequest =
                new HttpEntity<>(
                        createEventRequest,
                        organizerHeaders
                );

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        eventRequest,
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(
                eventResponse.getBody()
        );

        EventResponse createdEvent =
                eventResponse.getBody();

        assertNotNull(createdEvent.getId());

        // =========================================================
        // 5. REGISTER USER A
        // =========================================================

        String userAEmail =
                "user-a-" + UUID.randomUUID() + "@test.com";

        RegisterRequest userARegisterRequest =
                RegisterRequest.builder()
                        .name("E2E User A")
                        .email(userAEmail)
                        .password("password123")
                        .phoneNumber("9123456789")
                        .build();

        ResponseEntity<RegisterResponse> userARegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        userARegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userARegisterResponse.getStatusCode()
        );

        assertNotNull(
                userARegisterResponse.getBody()
        );

        Long userAId =
                userARegisterResponse.getBody().getId();

        assertNotNull(userAId);

        assertEquals(
                Role.USER,
                userARegisterResponse.getBody().getRole()
        );

        // =========================================================
        // 6. LOGIN USER A
        // =========================================================

        LoginRequest userALoginRequest =
                LoginRequest.builder()
                        .email(userAEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> userALoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        userALoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userALoginResponse.getStatusCode()
        );

        assertNotNull(
                userALoginResponse.getBody()
        );

        String userAToken =
                userALoginResponse.getBody().getToken();

        assertNotNull(userAToken);
        assertFalse(userAToken.isBlank());

        // =========================================================
        // 7. USER A CREATES BOOKING
        // =========================================================

        CreateBookingRequest createBookingRequest =
                CreateBookingRequest.builder()
                        .eventId(createdEvent.getId())
                        .numberOfTickets(2)
                        .build();

        HttpHeaders userAHeaders =
                new HttpHeaders();

        userAHeaders.setBearerAuth(userAToken);

        userAHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateBookingRequest> bookingRequest =
                new HttpEntity<>(
                        createBookingRequest,
                        userAHeaders
                );

        ResponseEntity<BookingResponse> bookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        bookingRequest,
                        BookingResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                bookingResponse.getStatusCode()
        );

        assertNotNull(
                bookingResponse.getBody()
        );

        BookingResponse userABooking =
                bookingResponse.getBody();

        assertNotNull(
                userABooking.getId()
        );

        assertEquals(
                userAId,
                userABooking.getUserId()
        );

        assertEquals(
                createdEvent.getId(),
                userABooking.getEventId()
        );

        assertEquals(
                BookingStatus.PENDING,
                userABooking.getBookingStatus()
        );

        // =========================================================
        // 8. REGISTER USER B
        // =========================================================

        String userBEmail =
                "user-b-" + UUID.randomUUID() + "@test.com";

        RegisterRequest userBRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E User B")
                        .email(userBEmail)
                        .password("password123")
                        .phoneNumber("9988776655")
                        .build();

        ResponseEntity<RegisterResponse> userBRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        userBRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userBRegisterResponse.getStatusCode()
        );

        assertNotNull(
                userBRegisterResponse.getBody()
        );

        assertEquals(
                Role.USER,
                userBRegisterResponse.getBody().getRole()
        );

        // =========================================================
        // 9. LOGIN USER B
        // =========================================================

        LoginRequest userBLoginRequest =
                LoginRequest.builder()
                        .email(userBEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> userBLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        userBLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userBLoginResponse.getStatusCode()
        );

        assertNotNull(
                userBLoginResponse.getBody()
        );

        String userBToken =
                userBLoginResponse.getBody().getToken();

        assertNotNull(userBToken);
        assertFalse(userBToken.isBlank());

        // =========================================================
        // 10. USER B TRIES TO ACCESS USER A'S BOOKING
        // =========================================================

        HttpHeaders userBHeaders =
                new HttpHeaders();

        userBHeaders.setBearerAuth(userBToken);

        HttpEntity<Void> unauthorizedRequest =
                new HttpEntity<>(userBHeaders);

        ResponseEntity<String> unauthorizedResponse =
                restTemplate.exchange(
                        "/api/bookings/"
                                + userABooking.getId(),
                        HttpMethod.GET,
                        unauthorizedRequest,
                        String.class
                );

        // =========================================================
        // 11. VERIFY BOOKING IS HIDDEN
        // =========================================================

        assertEquals(
                HttpStatus.NOT_FOUND,
                unauthorizedResponse.getStatusCode()
        );
    }
    @Test
    void organizer_shouldNotBeAbleToDeleteEventWithBookings() {

        // =========================================================
        // 1. REGISTER ORGANIZER
        // =========================================================

        String organizerEmail =
                "organizer-" + UUID.randomUUID() + "@test.com";

        RegisterRequest organizerRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Delete Organizer")
                        .email(organizerEmail)
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        ResponseEntity<RegisterResponse> organizerRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        organizerRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerRegisterResponse.getStatusCode()
        );

        // =========================================================
        // 2. PROMOTE ORGANIZER
        // =========================================================

        User organizer =
                userRepository.findByEmail(organizerEmail)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "Organizer was not found"
                                )
                        );

        organizer.setRole(Role.ORGANIZER);

        userRepository.save(organizer);

        // =========================================================
        // 3. LOGIN ORGANIZER
        // =========================================================

        LoginRequest organizerLoginRequest =
                LoginRequest.builder()
                        .email(organizerEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> organizerLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        organizerLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerLoginResponse.getStatusCode()
        );

        assertNotNull(
                organizerLoginResponse.getBody()
        );

        String organizerToken =
                organizerLoginResponse.getBody().getToken();

        assertNotNull(organizerToken);
        assertFalse(organizerToken.isBlank());

        // =========================================================
        // 4. ORGANIZER CREATES EVENT
        // =========================================================

        CreateEventRequest createEventRequest =
                CreateEventRequest.builder()
                        .title("E2E Event With Booking")
                        .description(
                                "Event that should not be deleted after booking"
                        )
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .capacity(100)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .category(EventCategory.CONCERT)
                        .build();

        HttpHeaders organizerHeaders =
                new HttpHeaders();

        organizerHeaders.setBearerAuth(
                organizerToken
        );

        organizerHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateEventRequest> eventRequest =
                new HttpEntity<>(
                        createEventRequest,
                        organizerHeaders
                );

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        eventRequest,
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(
                eventResponse.getBody()
        );

        EventResponse createdEvent =
                eventResponse.getBody();

        assertNotNull(createdEvent.getId());

        // =========================================================
        // 5. REGISTER USER
        // =========================================================

        String userEmail =
                "user-" + UUID.randomUUID() + "@test.com";

        RegisterRequest userRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Booking User")
                        .email(userEmail)
                        .password("password123")
                        .phoneNumber("9123456789")
                        .build();

        ResponseEntity<RegisterResponse> userRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        userRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userRegisterResponse.getStatusCode()
        );

        // =========================================================
        // 6. LOGIN USER
        // =========================================================

        LoginRequest userLoginRequest =
                LoginRequest.builder()
                        .email(userEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> userLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        userLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userLoginResponse.getStatusCode()
        );

        assertNotNull(
                userLoginResponse.getBody()
        );

        String userToken =
                userLoginResponse.getBody().getToken();

        assertNotNull(userToken);
        assertFalse(userToken.isBlank());

        // =========================================================
        // 7. USER CREATES BOOKING
        // =========================================================

        CreateBookingRequest createBookingRequest =
                CreateBookingRequest.builder()
                        .eventId(createdEvent.getId())
                        .numberOfTickets(2)
                        .build();

        HttpHeaders userHeaders =
                new HttpHeaders();

        userHeaders.setBearerAuth(userToken);

        userHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateBookingRequest> bookingRequest =
                new HttpEntity<>(
                        createBookingRequest,
                        userHeaders
                );

        ResponseEntity<BookingResponse> bookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        bookingRequest,
                        BookingResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                bookingResponse.getStatusCode()
        );

        assertNotNull(
                bookingResponse.getBody()
        );

        BookingResponse createdBooking =
                bookingResponse.getBody();

        assertNotNull(createdBooking.getId());

        assertEquals(
                createdEvent.getId(),
                createdBooking.getEventId()
        );

        // Booking exists, even though it is PENDING.
        assertEquals(
                BookingStatus.PENDING,
                createdBooking.getBookingStatus()
        );

        // =========================================================
        // 8. ORGANIZER TRIES TO DELETE EVENT
        // =========================================================

        HttpEntity<Void> deleteRequest =
                new HttpEntity<>(organizerHeaders);

        ResponseEntity<String> deleteResponse =
                restTemplate.exchange(
                        "/api/events/"
                                + createdEvent.getId(),
                        HttpMethod.DELETE,
                        deleteRequest,
                        String.class
                );

        // =========================================================
        // 9. VERIFY DELETION IS BLOCKED
        // =========================================================

        assertEquals(
                HttpStatus.CONFLICT,
                deleteResponse.getStatusCode()
        );

        assertNotNull(deleteResponse.getBody());

        assertTrue(
                deleteResponse.getBody()
                        .contains("cannot be deleted because it has bookings")
        );
    }
    @Test
    void user_shouldNotBeAbleToBookBeyondEventCapacity() {

        // =========================================================
        // 1. REGISTER ORGANIZER
        // =========================================================

        String organizerEmail =
                "organizer-" + UUID.randomUUID() + "@test.com";

        RegisterRequest organizerRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Capacity Organizer")
                        .email(organizerEmail)
                        .password("password123")
                        .phoneNumber("9876543210")
                        .build();

        ResponseEntity<RegisterResponse> organizerRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        organizerRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerRegisterResponse.getStatusCode()
        );

        // =========================================================
        // 2. PROMOTE ORGANIZER
        // =========================================================

        User organizer =
                userRepository.findByEmail(organizerEmail)
                        .orElseThrow(
                                () -> new AssertionError(
                                        "Organizer was not found"
                                )
                        );

        organizer.setRole(Role.ORGANIZER);

        userRepository.save(organizer);

        // =========================================================
        // 3. LOGIN ORGANIZER
        // =========================================================

        LoginRequest organizerLoginRequest =
                LoginRequest.builder()
                        .email(organizerEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> organizerLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        organizerLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                organizerLoginResponse.getStatusCode()
        );

        assertNotNull(
                organizerLoginResponse.getBody()
        );

        String organizerToken =
                organizerLoginResponse.getBody().getToken();

        assertNotNull(organizerToken);
        assertFalse(organizerToken.isBlank());

        // =========================================================
        // 4. CREATE EVENT WITH CAPACITY = 2
        // =========================================================

        CreateEventRequest createEventRequest =
                CreateEventRequest.builder()
                        .title("E2E Limited Capacity Event")
                        .description(
                                "Event for capacity testing"
                        )
                        .location("Lucknow")
                        .eventDate(
                                LocalDateTime.now().plusDays(30)
                        )
                        .capacity(2)
                        .ticketPrice(
                                new BigDecimal("500.00")
                        )
                        .category(EventCategory.CONCERT)
                        .build();

        HttpHeaders organizerHeaders =
                new HttpHeaders();

        organizerHeaders.setBearerAuth(
                organizerToken
        );

        organizerHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateEventRequest> eventRequest =
                new HttpEntity<>(
                        createEventRequest,
                        organizerHeaders
                );

        ResponseEntity<EventResponse> eventResponse =
                restTemplate.exchange(
                        "/api/events",
                        HttpMethod.POST,
                        eventRequest,
                        EventResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                eventResponse.getStatusCode()
        );

        assertNotNull(
                eventResponse.getBody()
        );

        EventResponse createdEvent =
                eventResponse.getBody();

        assertNotNull(createdEvent.getId());

        assertEquals(
                2,
                createdEvent.getCapacity()
        );

        // =========================================================
        // 5. REGISTER USER A
        // =========================================================

        String userAEmail =
                "user-a-" + UUID.randomUUID() + "@test.com";

        RegisterRequest userARegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Capacity User A")
                        .email(userAEmail)
                        .password("password123")
                        .phoneNumber("9123456789")
                        .build();

        ResponseEntity<RegisterResponse> userARegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        userARegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userARegisterResponse.getStatusCode()
        );

        // =========================================================
        // 6. LOGIN USER A
        // =========================================================

        LoginRequest userALoginRequest =
                LoginRequest.builder()
                        .email(userAEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> userALoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        userALoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userALoginResponse.getStatusCode()
        );

        assertNotNull(
                userALoginResponse.getBody()
        );

        String userAToken =
                userALoginResponse.getBody().getToken();

        assertNotNull(userAToken);
        assertFalse(userAToken.isBlank());

        // =========================================================
        // 7. USER A BOOKS ALL 2 AVAILABLE TICKETS
        // =========================================================

        CreateBookingRequest userABookingRequest =
                CreateBookingRequest.builder()
                        .eventId(createdEvent.getId())
                        .numberOfTickets(2)
                        .build();

        HttpHeaders userAHeaders =
                new HttpHeaders();

        userAHeaders.setBearerAuth(userAToken);

        userAHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateBookingRequest> userABookingHttpRequest =
                new HttpEntity<>(
                        userABookingRequest,
                        userAHeaders
                );

        ResponseEntity<BookingResponse> userABookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        userABookingHttpRequest,
                        BookingResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userABookingResponse.getStatusCode()
        );

        assertNotNull(
                userABookingResponse.getBody()
        );

        BookingResponse userABooking =
                userABookingResponse.getBody();

        assertNotNull(userABooking.getId());

        assertEquals(
                2,
                userABooking.getNumberOfTickets()
        );

        assertEquals(
                BookingStatus.PENDING,
                userABooking.getBookingStatus()
        );

        // =========================================================
        // 8. CREATE PAYMENT FOR USER A
        // =========================================================

        CreatePaymentRequest paymentRequest =
                new CreatePaymentRequest(
                        userABooking.getId()
                );

        HttpHeaders paymentHeaders =
                new HttpHeaders();

        paymentHeaders.setBearerAuth(userAToken);

        paymentHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreatePaymentRequest> paymentHttpRequest =
                new HttpEntity<>(
                        paymentRequest,
                        paymentHeaders
                );

        ResponseEntity<PaymentResponse> paymentResponse =
                restTemplate.exchange(
                        "/api/payments",
                        HttpMethod.POST,
                        paymentHttpRequest,
                        PaymentResponse.class
                );

        assertEquals(
                HttpStatus.CREATED,
                paymentResponse.getStatusCode()
        );

        assertNotNull(
                paymentResponse.getBody()
        );

        PaymentResponse createdPayment =
                paymentResponse.getBody();

        assertNotNull(createdPayment.getId());

        assertEquals(
                userABooking.getId(),
                createdPayment.getBookingId()
        );

        assertEquals(
                PaymentStatus.PENDING,
                createdPayment.getPaymentStatus()
        );

        // =========================================================
        // 9. COMPLETE USER A'S PAYMENT
        // =========================================================

        HttpEntity<Void> successPaymentRequest =
                new HttpEntity<>(paymentHeaders);

        ResponseEntity<PaymentResponse> successPaymentResponse =
                restTemplate.exchange(
                        "/api/payments/"
                                + createdPayment.getId()
                                + "/success",
                        HttpMethod.PATCH,
                        successPaymentRequest,
                        PaymentResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                successPaymentResponse.getStatusCode()
        );

        assertNotNull(
                successPaymentResponse.getBody()
        );

        assertEquals(
                PaymentStatus.SUCCESS,
                successPaymentResponse
                        .getBody()
                        .getPaymentStatus()
        );

        // =========================================================
        // 10. REGISTER USER B
        // =========================================================

        String userBEmail =
                "user-b-" + UUID.randomUUID() + "@test.com";

        RegisterRequest userBRegisterRequest =
                RegisterRequest.builder()
                        .name("E2E Capacity User B")
                        .email(userBEmail)
                        .password("password123")
                        .phoneNumber("9988776655")
                        .build();

        ResponseEntity<RegisterResponse> userBRegisterResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        userBRegisterRequest,
                        RegisterResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userBRegisterResponse.getStatusCode()
        );

        // =========================================================
        // 11. LOGIN USER B
        // =========================================================

        LoginRequest userBLoginRequest =
                LoginRequest.builder()
                        .email(userBEmail)
                        .password("password123")
                        .build();

        ResponseEntity<AuthResponse> userBLoginResponse =
                restTemplate.postForEntity(
                        "/api/auth/login",
                        userBLoginRequest,
                        AuthResponse.class
                );

        assertEquals(
                HttpStatus.OK,
                userBLoginResponse.getStatusCode()
        );

        assertNotNull(
                userBLoginResponse.getBody()
        );

        String userBToken =
                userBLoginResponse.getBody().getToken();

        assertNotNull(userBToken);
        assertFalse(userBToken.isBlank());

        // =========================================================
        // 12. USER B TRIES TO BOOK 1 MORE TICKET
        // =========================================================

        CreateBookingRequest userBBookingRequest =
                CreateBookingRequest.builder()
                        .eventId(createdEvent.getId())
                        .numberOfTickets(1)
                        .build();

        HttpHeaders userBHeaders =
                new HttpHeaders();

        userBHeaders.setBearerAuth(userBToken);

        userBHeaders.setContentType(
                MediaType.APPLICATION_JSON
        );

        HttpEntity<CreateBookingRequest> userBBookingHttpRequest =
                new HttpEntity<>(
                        userBBookingRequest,
                        userBHeaders
                );

        ResponseEntity<String> userBBookingResponse =
                restTemplate.exchange(
                        "/api/bookings",
                        HttpMethod.POST,
                        userBBookingHttpRequest,
                        String.class
                );

        // =========================================================
        // 13. VERIFY CAPACITY IS ENFORCED
        // =========================================================

        assertEquals(
                HttpStatus.CONFLICT,
                userBBookingResponse.getStatusCode()
        );

        assertNotNull(
                userBBookingResponse.getBody()
        );

        assertTrue(
                userBBookingResponse
                        .getBody()
                        .contains(
                                "Requested tickets exceed available event capacity."
                        )
        );
    }
}