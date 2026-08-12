package com.eventsphere.eventsphere_backend.booking.service;

import com.eventsphere.eventsphere_backend.booking.dto.BookingResponse;
import com.eventsphere.eventsphere_backend.booking.dto.CreateBookingRequest;
import com.eventsphere.eventsphere_backend.booking.entity.Booking;
import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import com.eventsphere.eventsphere_backend.booking.mapper.BookingMapper;
import com.eventsphere.eventsphere_backend.booking.repository.BookingRepository;
import com.eventsphere.eventsphere_backend.common.exception.*;
import com.eventsphere.eventsphere_backend.event.entity.Event;
import com.eventsphere.eventsphere_backend.event.repository.EventRepository;
import com.eventsphere.eventsphere_backend.notification.service.NotificationService;
import com.eventsphere.eventsphere_backend.user.entity.User;
import com.eventsphere.eventsphere_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

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
    private final NotificationService notificationService;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            BookingMapper bookingMapper,
            NotificationService notificationService) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.bookingMapper = bookingMapper;
        this.notificationService = notificationService;
    }

    // Create Booking
    public BookingResponse createBooking(
            CreateBookingRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new EventNotFoundException(request.getEventId()));

        // Calculate booked and available seats
        Integer bookedTickets =
                bookingRepository.getBookedTickets(event.getId());

        int availableSeats =
                event.getCapacity() - bookedTickets;

        // Validate requested tickets
        if (request.getNumberOfTickets() > availableSeats) {
            throw new EventCapacityExceededException();
        }

        BigDecimal totalAmount = event.getTicketPrice()
                .multiply(
                        BigDecimal.valueOf(
                                request.getNumberOfTickets()
                        )
                );

        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .numberOfTickets(request.getNumberOfTickets())
                .totalAmount(totalAmount)
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingDate(LocalDateTime.now())
                .user(user)
                .event(event)
                .build();

        Booking savedBooking =
                bookingRepository.save(booking);

        // Create booking confirmation notification
        notificationService.createNotification(
                user.getId(),
                "Booking Confirmed",
                "Your booking for "
                        + event.getTitle()
                        + " has been confirmed."
        );

        return bookingMapper.toResponse(savedBooking);
    }

    // Get All Bookings
    public List<BookingResponse> getAllBookings() {

        return bookingRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get My Bookings
    public List<BookingResponse> getMyBookings(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        return bookingRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Get Booking By ID
    public BookingResponse getBookingById(
            Long id,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new BookingNotFoundException(id));

        // Check booking ownership
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BookingNotFoundException(id);
        }

        return bookingMapper.toResponse(booking);
    }

    // Cancel Booking
    public void cancelBooking(
            Long id,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new BookingNotFoundException(id));

        // Check booking ownership
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BookingNotFoundException(id);
        }

        // Prevent cancelling an already cancelled booking
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new BookingAlreadyCancelledException(id);
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);
    }

    // Generate Booking Reference
    private String generateBookingReference() {

        return "EVT-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();
    }
}