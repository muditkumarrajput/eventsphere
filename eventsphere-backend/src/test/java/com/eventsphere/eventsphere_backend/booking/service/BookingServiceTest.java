package com.eventsphere.eventsphere_backend.booking.service;

import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.dto.CreateBookingRequest;
import com.eventsphere.eventsphere_backend.booking.entity.Booking;
import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import com.eventsphere.eventsphere_backend.booking.mapper.BookingMapper;
import com.eventsphere.eventsphere_backend.booking.repository.BookingRepository;
import com.eventsphere.eventsphere_backend.common.exception.BookingAlreadyCancelledException;
import com.eventsphere.eventsphere_backend.common.exception.BookingNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.EventCapacityExceededException;
import com.eventsphere.eventsphere_backend.common.exception.EventNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.UserNotFoundException;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;


    // =========================================================
    // CREATE BOOKING
    // =========================================================

    @Test
    void shouldCreateBookingSuccessfully() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);
        event.setCapacity(100);
        event.setTicketPrice(new BigDecimal("1499.00"));

        CreateBookingRequest request =
                CreateBookingRequest.builder()
                        .eventId(3L)
                        .numberOfTickets(2)
                        .build();

        Booking savedBooking = Booking.builder()
                .id(1L)
                .bookingReference("EVT-ABC12345")
                .numberOfTickets(2)
                .totalAmount(new BigDecimal("2998.00"))
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .event(event)
                .build();

        BookingResponse response =
                BookingResponse.builder()
                        .id(1L)
                        .bookingReference("EVT-ABC12345")
                        .numberOfTickets(2)
                        .totalAmount(new BigDecimal("2998.00"))
                        .bookingStatus(BookingStatus.PENDING)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        when(bookingRepository.getBookedTickets(3L))
                .thenReturn(10);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(savedBooking);

        when(bookingMapper.toResponse(savedBooking))
                .thenReturn(response);

        // Act
        BookingResponse result =
                bookingService.createBooking(request, email);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("EVT-ABC12345", result.getBookingReference());
        assertEquals(2, result.getNumberOfTickets());
        assertEquals(
                new BigDecimal("2998.00"),
                result.getTotalAmount()
        );
        assertEquals(
                BookingStatus.PENDING,
                result.getBookingStatus()
        );

        verify(bookingRepository)
                .getBookedTickets(3L);

        verify(bookingRepository)
                .save(any(Booking.class));

        verify(bookingMapper)
                .toResponse(savedBooking);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistDuringBooking() {

        // Arrange
        String email = "unknown@test.com";

        CreateBookingRequest request =
                CreateBookingRequest.builder()
                        .eventId(3L)
                        .numberOfTickets(2)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        UserNotFoundException exception =
                assertThrows(
                        UserNotFoundException.class,
                        () -> bookingService.createBooking(
                                request,
                                email
                        )
                );

        assertEquals(
                "User with email unknown@test.com not found.",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(eventRepository);
        verifyNoInteractions(bookingRepository);
    }


    @Test
    void shouldThrowExceptionWhenEventDoesNotExistDuringBooking() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        CreateBookingRequest request =
                CreateBookingRequest.builder()
                        .eventId(99L)
                        .numberOfTickets(2)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act + Assert
        EventNotFoundException exception =
                assertThrows(
                        EventNotFoundException.class,
                        () -> bookingService.createBooking(
                                request,
                                email
                        )
                );

        assertEquals(
                "Event with id 99 not found.",
                exception.getMessage()
        );

        verify(eventRepository)
                .findById(99L);

        verifyNoInteractions(bookingRepository);
    }


    @Test
    void shouldThrowExceptionWhenBookingExceedsCapacity() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);
        event.setCapacity(10);
        event.setTicketPrice(new BigDecimal("1000.00"));

        CreateBookingRequest request =
                CreateBookingRequest.builder()
                        .eventId(3L)
                        .numberOfTickets(6)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findById(3L))
                .thenReturn(Optional.of(event));

        // 8 already booked -> only 2 seats available
        when(bookingRepository.getBookedTickets(3L))
                .thenReturn(8);

        // Act + Assert
        assertThrows(
                EventCapacityExceededException.class,
                () -> bookingService.createBooking(
                        request,
                        email
                )
        );

        verify(bookingRepository)
                .getBookedTickets(3L);

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }


    // =========================================================
    // GET ALL BOOKINGS
    // =========================================================

    @Test
    void shouldGetAllBookings() {

        // Arrange
        Booking booking1 = Booking.builder()
                .id(1L)
                .build();

        Booking booking2 = Booking.builder()
                .id(2L)
                .build();

        BookingResponse response1 =
                BookingResponse.builder()
                        .id(1L)
                        .build();

        BookingResponse response2 =
                BookingResponse.builder()
                        .id(2L)
                        .build();

        when(bookingRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(booking1, booking2));

        when(bookingMapper.toResponse(booking1))
                .thenReturn(response1);

        when(bookingMapper.toResponse(booking2))
                .thenReturn(response2);

        // Act
        List<BookingResponse> result =
                bookingService.getAllBookings();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(bookingRepository)
                .findAllByOrderByCreatedAtDesc();

        verify(bookingMapper)
                .toResponse(booking1);

        verify(bookingMapper)
                .toResponse(booking2);
    }


    // =========================================================
    // GET MY BOOKINGS
    // =========================================================

    @Test
    void shouldGetMyBookings() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Booking booking = Booking.builder()
                .id(1L)
                .user(user)
                .build();

        BookingResponse response =
                BookingResponse.builder()
                        .id(1L)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(booking));

        when(bookingMapper.toResponse(booking))
                .thenReturn(response);

        // Act
        List<BookingResponse> result =
                bookingService.getMyBookings(email);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(userRepository)
                .findByEmail(email);

        verify(bookingRepository)
                .findByUserOrderByCreatedAtDesc(user);

        verify(bookingMapper)
                .toResponse(booking);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistForMyBookings() {

        // Arrange
        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getMyBookings(email)
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(bookingRepository);
    }


    // =========================================================
    // GET BOOKING BY ID
    // =========================================================

    @Test
    void shouldGetBookingByIdWhenUserOwnsBooking() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Booking booking = Booking.builder()
                .id(1L)
                .user(user)
                .build();

        BookingResponse response =
                BookingResponse.builder()
                        .id(1L)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(bookingMapper.toResponse(booking))
                .thenReturn(response);

        // Act
        BookingResponse result =
                bookingService.getBookingById(1L, email);

        // Assert
        assertEquals(1L, result.getId());

        verify(bookingRepository)
                .findById(1L);

        verify(bookingMapper)
                .toResponse(booking);
    }


    @Test
    void shouldThrowExceptionWhenAnotherUserTriesToViewBooking() {

        // Arrange
        String email = "another@test.com";

        User anotherUser = new User();
        anotherUser.setId(10L);

        User bookingOwner = new User();
        bookingOwner.setId(5L);

        Booking booking = Booking.builder()
                .id(1L)
                .user(bookingOwner)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(anotherUser));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Act + Assert
        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getBookingById(1L, email)
        );

        verify(bookingRepository)
                .findById(1L);

        verifyNoInteractions(bookingMapper);
    }


    @Test
    void shouldThrowExceptionWhenBookingDoesNotExist() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getBookingById(99L, email)
        );

        verify(bookingRepository)
                .findById(99L);
    }


    // =========================================================
    // CANCEL BOOKING
    // =========================================================

    @Test
    void shouldCancelBookingSuccessfully() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Booking booking = Booking.builder()
                .id(1L)
                .user(user)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Act
        bookingService.cancelBooking(1L, email);

        // Assert
        assertEquals(
                BookingStatus.CANCELLED,
                booking.getBookingStatus()
        );

        verify(bookingRepository)
                .save(booking);
    }


    @Test
    void shouldThrowExceptionWhenBookingAlreadyCancelled() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Booking booking = Booking.builder()
                .id(1L)
                .user(user)
                .bookingStatus(BookingStatus.CANCELLED)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Act + Assert
        assertThrows(
                BookingAlreadyCancelledException.class,
                () -> bookingService.cancelBooking(1L, email)
        );

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }


    @Test
    void shouldThrowExceptionWhenAnotherUserTriesToCancelBooking() {

        // Arrange
        String email = "another@test.com";

        User anotherUser = new User();
        anotherUser.setId(10L);

        User bookingOwner = new User();
        bookingOwner.setId(5L);

        Booking booking = Booking.builder()
                .id(1L)
                .user(bookingOwner)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(anotherUser));

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        // Act + Assert
        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.cancelBooking(1L, email)
        );

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }


    @Test
    void shouldThrowExceptionWhenBookingDoesNotExistDuringCancellation() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(bookingRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.cancelBooking(99L, email)
        );

        verify(bookingRepository)
                .findById(99L);

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }
}