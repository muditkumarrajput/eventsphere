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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

        BookingResponse response =
                BookingResponse.builder()
                        .id(1L)
                        .numberOfTickets(2)
                        .totalAmount(new BigDecimal("2998.00"))
                        .bookingStatus(BookingStatus.PENDING)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByIdForUpdate(3L))
                .thenReturn(Optional.of(event));

        when(bookingRepository.getBookedTickets(3L))
                .thenReturn(10);

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking booking = invocation.getArgument(0);
                    booking.setId(1L);
                    return booking;
                });

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(response);

        // Act
        BookingResponse result =
                bookingService.createBooking(request, email);

        // Assert
        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                2,
                result.getNumberOfTickets()
        );

        assertEquals(
                new BigDecimal("2998.00"),
                result.getTotalAmount()
        );

        assertEquals(
                BookingStatus.PENDING,
                result.getBookingStatus()
        );

        // Capture the Booking object created by the service
        ArgumentCaptor<Booking> bookingCaptor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository)
                .save(bookingCaptor.capture());

        Booking savedBooking =
                bookingCaptor.getValue();

        // Verify calculated booking data
        assertEquals(
                2,
                savedBooking.getNumberOfTickets()
        );

        assertEquals(
                new BigDecimal("2998.00"),
                savedBooking.getTotalAmount()
        );

        assertEquals(
                BookingStatus.PENDING,
                savedBooking.getBookingStatus()
        );

        assertEquals(
                user,
                savedBooking.getUser()
        );

        assertEquals(
                event,
                savedBooking.getEvent()
        );

        assertNotNull(
                savedBooking.getBookingDate()
        );

        // Verify booking reference format
        assertNotNull(
                savedBooking.getBookingReference()
        );

        assertTrue(
                savedBooking.getBookingReference()
                        .matches("EVT-[A-Z0-9]{8}")
        );

        verify(userRepository)
                .findByEmail(email);

        verify(eventRepository)
                .findByIdForUpdate(3L);

        verify(bookingRepository)
                .getBookedTickets(3L);

        verify(bookingMapper)
                .toResponse(savedBooking);
    }


    @Test
    void shouldCalculateCorrectTotalAmountWhenCreatingBooking() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);
        event.setCapacity(100);
        event.setTicketPrice(new BigDecimal("750.50"));

        CreateBookingRequest request =
                CreateBookingRequest.builder()
                        .eventId(3L)
                        .numberOfTickets(4)
                        .build();

        BookingResponse response =
                BookingResponse.builder()
                        .id(1L)
                        .totalAmount(new BigDecimal("3002.00"))
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByIdForUpdate(3L))
                .thenReturn(Optional.of(event));

        when(bookingRepository.getBookedTickets(3L))
                .thenReturn(20);

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(response);

        // Act
        bookingService.createBooking(request, email);

        // Assert
        ArgumentCaptor<Booking> bookingCaptor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository)
                .save(bookingCaptor.capture());

        Booking booking =
                bookingCaptor.getValue();

        assertEquals(
                new BigDecimal("3002.00"),
                booking.getTotalAmount()
        );

        assertEquals(
                4,
                booking.getNumberOfTickets()
        );
    }


    @Test
    void shouldGenerateUniqueBookingReference() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);
        event.setCapacity(100);
        event.setTicketPrice(new BigDecimal("1000.00"));

        CreateBookingRequest request =
                CreateBookingRequest.builder()
                        .eventId(3L)
                        .numberOfTickets(1)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByIdForUpdate(3L))
                .thenReturn(Optional.of(event));

        when(bookingRepository.getBookedTickets(3L))
                .thenReturn(0);

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(
                        BookingResponse.builder().build()
                );

        // Act
        bookingService.createBooking(request, email);

        // Assert
        ArgumentCaptor<Booking> bookingCaptor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository)
                .save(bookingCaptor.capture());

        Booking booking =
                bookingCaptor.getValue();

        String reference =
                booking.getBookingReference();

        assertNotNull(reference);

        assertTrue(
                reference.startsWith("EVT-")
        );

        assertEquals(
                12,
                reference.length()
        );

        assertTrue(
                reference.matches("EVT-[A-Z0-9]{8}")
        );
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
        verifyNoInteractions(bookingMapper);
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

        when(eventRepository.findByIdForUpdate(99L))
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

        verify(userRepository)
                .findByEmail(email);

        verify(eventRepository)
                .findByIdForUpdate(99L);

        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(bookingMapper);
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

        when(eventRepository.findByIdForUpdate(3L))
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

        verify(eventRepository)
                .findByIdForUpdate(3L);

        verify(bookingRepository)
                .getBookedTickets(3L);

        verify(bookingRepository, never())
                .save(any(Booking.class));

        verifyNoInteractions(bookingMapper);
    }


    @Test
    void shouldCreateBookingWhenRequestedTicketsExactlyMatchAvailableSeats() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);

        Event event = new Event();
        event.setId(3L);
        event.setCapacity(10);
        event.setTicketPrice(new BigDecimal("500.00"));

        CreateBookingRequest request =
                CreateBookingRequest.builder()
                        .eventId(3L)
                        .numberOfTickets(2)
                        .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(eventRepository.findByIdForUpdate(3L))
                .thenReturn(Optional.of(event));

        // 8 booked -> exactly 2 seats available
        when(bookingRepository.getBookedTickets(3L))
                .thenReturn(8);

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(bookingMapper.toResponse(any(Booking.class)))
                .thenReturn(
                        BookingResponse.builder()
                                .id(1L)
                                .build()
                );

        // Act
        BookingResponse result =
                bookingService.createBooking(request, email);

        // Assert
        assertNotNull(result);

        verify(bookingRepository)
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
        assertNotNull(result);

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                1L,
                result.get(0).getId()
        );

        assertEquals(
                2L,
                result.get(1).getId()
        );

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
        assertNotNull(result);

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                1L,
                result.get(0).getId()
        );

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
        verifyNoInteractions(bookingMapper);
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
        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        verify(userRepository)
                .findByEmail(email);

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
                () -> bookingService.getBookingById(
                        1L,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

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
                () -> bookingService.getBookingById(
                        99L,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verify(bookingRepository)
                .findById(99L);

        verifyNoInteractions(bookingMapper);
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistWhileGettingBooking() {

        // Arrange
        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getBookingById(
                        1L,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(bookingMapper);
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
        bookingService.cancelBooking(
                1L,
                email
        );

        // Assert
        assertEquals(
                BookingStatus.CANCELLED,
                booking.getBookingStatus()
        );

        verify(userRepository)
                .findByEmail(email);

        verify(bookingRepository)
                .findById(1L);

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
                () -> bookingService.cancelBooking(
                        1L,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verify(bookingRepository)
                .findById(1L);

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
                () -> bookingService.cancelBooking(
                        1L,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verify(bookingRepository)
                .findById(1L);

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
                () -> bookingService.cancelBooking(
                        99L,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verify(bookingRepository)
                .findById(99L);

        verify(bookingRepository, never())
                .save(any(Booking.class));
    }


    @Test
    void shouldThrowExceptionWhenUserDoesNotExistDuringCancellation() {

        // Arrange
        String email = "unknown@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                UserNotFoundException.class,
                () -> bookingService.cancelBooking(
                        1L,
                        email
                )
        );

        verify(userRepository)
                .findByEmail(email);

        verifyNoInteractions(bookingRepository);
    }
}