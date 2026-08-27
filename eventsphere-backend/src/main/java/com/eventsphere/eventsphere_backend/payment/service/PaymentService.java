package com.eventsphere.eventsphere_backend.payment.service;

import com.eventsphere.eventsphere_backend.booking.entity.Booking;
import com.eventsphere.eventsphere_backend.booking.entity.BookingStatus;
import com.eventsphere.eventsphere_backend.booking.repository.BookingRepository;
import com.eventsphere.eventsphere_backend.common.exception.BookingNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.PaymentAlreadyExistsException;
import com.eventsphere.eventsphere_backend.common.exception.PaymentNotFoundException;
import com.eventsphere.eventsphere_backend.common.exception.PaymentStateTransitionException;
import com.eventsphere.eventsphere_backend.notification.service.NotificationService;
import com.eventsphere.eventsphere_backend.payment.dto.CreatePaymentRequest;
import com.eventsphere.eventsphere_backend.payment.dto.PaymentResponse;
import com.eventsphere.eventsphere_backend.payment.entity.Payment;
import com.eventsphere.eventsphere_backend.payment.entity.PaymentStatus;
import com.eventsphere.eventsphere_backend.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            NotificationService notificationService) {

        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    public PaymentResponse createPayment(
            CreatePaymentRequest request,
            String email) {

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() ->
                        new BookingNotFoundException(request.bookingId()));

        // Only booking owner can create payment
        verifyBookingOwnership(booking, email);

        // Prevent duplicate payment for the same booking
        if (paymentRepository.existsByBooking(booking)) {
            throw new PaymentAlreadyExistsException(
                    request.bookingId()
            );
        }

        Payment payment = Payment.builder()
                .paymentReference(generatePaymentReference())
                .amount(booking.getTotalAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .booking(booking)
                .build();

        Payment savedPayment =
                paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    // =========================================================
    // GET PAYMENT BY ID
    // =========================================================

    public PaymentResponse getPaymentById(
            Long id,
            String email) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(id));

        // Only booking owner can view payment
        verifyBookingOwnership(
                payment.getBooking(),
                email
        );

        return toResponse(payment);
    }

    // =========================================================
    // MARK PAYMENT AS SUCCESSFUL
    // =========================================================

    @Transactional
    public PaymentResponse markPaymentSuccessful(
            Long id,
            String email) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(id));

        Booking booking = payment.getBooking();

        // Only booking owner can update payment
        verifyBookingOwnership(booking, email);

        // Only PENDING payment can become SUCCESS
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {

            throw new PaymentStateTransitionException(
                    id,
                    payment.getPaymentStatus().name(),
                    PaymentStatus.SUCCESS.name()
            );
        }

        // Update payment status
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        // Confirm booking
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        bookingRepository.save(booking);

        Payment savedPayment =
                paymentRepository.save(payment);

        // Create notification
        notificationService.createNotification(
                booking.getUser().getId(),
                "Payment Successful",
                "Your payment for booking "
                        + booking.getBookingReference()
                        + " was successful."
        );

        return toResponse(savedPayment);
    }

    // =========================================================
    // MARK PAYMENT AS FAILED
    // =========================================================

    @Transactional
    public PaymentResponse markPaymentFailed(
            Long id,
            String email) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(id));

        Booking booking = payment.getBooking();

        // Only booking owner can update payment
        verifyBookingOwnership(booking, email);

        // Only PENDING payment can become FAILED
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {

            throw new PaymentStateTransitionException(
                    id,
                    payment.getPaymentStatus().name(),
                    PaymentStatus.FAILED.name()
            );
        }

        // Update payment status
        payment.setPaymentStatus(PaymentStatus.FAILED);

        // Cancel booking
        booking.setBookingStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);

        Payment savedPayment =
                paymentRepository.save(payment);

        // Create notification
        notificationService.createNotification(
                booking.getUser().getId(),
                "Payment Failed",
                "Your payment for booking "
                        + booking.getBookingReference()
                        + " failed. Your booking has been cancelled."
        );

        return toResponse(savedPayment);
    }

    // =========================================================
    // VERIFY BOOKING OWNERSHIP
    // =========================================================

    private void verifyBookingOwnership(
            Booking booking,
            String email) {

        if (!booking.getUser().getEmail().equals(email)) {

            // Hide existence of another user's booking
            throw new BookingNotFoundException(
                    booking.getId()
            );
        }
    }

    // =========================================================
    // ENTITY → RESPONSE
    // =========================================================

    private PaymentResponse toResponse(
            Payment payment) {

        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    // =========================================================
    // GENERATE PAYMENT REFERENCE
    // =========================================================

    private String generatePaymentReference() {

        return "PAY-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}