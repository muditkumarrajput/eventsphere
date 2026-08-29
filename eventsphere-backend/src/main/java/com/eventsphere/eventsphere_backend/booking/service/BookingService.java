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
import com.eventsphere.eventsphere_backend.payment.service.PaymentService;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingMapper bookingMapper;
    private final PaymentService paymentService;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            BookingMapper bookingMapper,
            PaymentService paymentService) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.bookingMapper = bookingMapper;
        this.paymentService = paymentService;
    }

    // =========================================================
    // CREATE BOOKING
    // =========================================================

    @Transactional
    public BookingResponse createBooking(
            CreateBookingRequest request,
            String email) {

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        // Lock event row while checking capacity
        Event event = eventRepository.findByIdForUpdate(
                        request.getEventId()
                )
                .orElseThrow(() ->
                        new EventNotFoundException(
                                request.getEventId()
                        ));

        // Calculate currently booked tickets
        Integer bookedTickets =
                bookingRepository.getBookedTickets(event.getId());

        // Calculate available seats
        int availableSeats =
                event.getCapacity() - bookedTickets;

        // Validate requested tickets
        if (request.getNumberOfTickets() > availableSeats) {

            throw new EventCapacityExceededException();
        }

        // Calculate total amount
        BigDecimal totalAmount =
                event.getTicketPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        request.getNumberOfTickets()
                                )
                        );

        // Create booking
        Booking booking = Booking.builder()
                .bookingReference(
                        generateBookingReference()
                )
                .numberOfTickets(
                        request.getNumberOfTickets()
                )
                .totalAmount(totalAmount)

                // Booking remains pending until payment succeeds
                .bookingStatus(BookingStatus.PENDING)

                .bookingDate(LocalDateTime.now())
                .user(user)
                .event(event)
                .build();

        // Save booking
        Booking savedBooking =
                bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    // =========================================================
    // GET ALL BOOKINGS
    // =========================================================

    public List<BookingResponse> getAllBookings() {

        return bookingRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET MY BOOKINGS
    // =========================================================

    public List<BookingResponse> getMyBookings(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return bookingRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =========================================================
    // GET BOOKING BY ID
    // =========================================================

    public BookingResponse getBookingById(
            Long id,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new BookingNotFoundException(id));

        // Ownership check
        if (!booking.getUser().getId()
                .equals(user.getId())) {

            // Hide another user's booking
            throw new BookingNotFoundException(id);
        }

        return bookingMapper.toResponse(booking);
    }

    // =========================================================
    // CANCEL BOOKING
    // =========================================================

    @Transactional
    public void cancelBooking(
            Long id,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new BookingNotFoundException(id));

        // Ownership check
        if (!booking.getUser().getId()
                .equals(user.getId())) {

            // Hide another user's booking
            throw new BookingNotFoundException(id);
        }

        // Prevent duplicate cancellation
        if (booking.getBookingStatus()
                == BookingStatus.CANCELLED) {

            throw new BookingAlreadyCancelledException(id);
        }

        /*
         * If payment was successful,
         * refund the payment before cancelling the booking.
         */
        if (booking.getBookingStatus()
                == BookingStatus.CONFIRMED) {

            paymentService.refundPayment(
                    booking.getId(),
                    email
            );
        }

        // Cancel booking
        booking.setBookingStatus(
                BookingStatus.CANCELLED
        );

        bookingRepository.save(booking);
    }

    // =========================================================
    // GENERATE BOOKING REFERENCE
    // =========================================================

    private String generateBookingReference() {

        return "EVT-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();
    }
}