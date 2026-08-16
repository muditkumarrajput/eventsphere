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

    // Create Payment
    public PaymentResponse createPayment(
            CreatePaymentRequest request,
            String email) {

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() ->
                        new BookingNotFoundException(request.bookingId()));

        verifyBookingOwnership(booking, email);

        if (paymentRepository.existsByBooking(booking)) {
            throw new PaymentAlreadyExistsException(request.bookingId());
        }

        Payment payment = Payment.builder()
                .paymentReference(generatePaymentReference())
                .amount(booking.getTotalAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .booking(booking)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    // Get Payment by ID
    public PaymentResponse getPaymentById(
            Long id,
            String email) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(id));

        verifyBookingOwnership(
                payment.getBooking(),
                email
        );

        return toResponse(payment);
    }

    // Mark Payment as Successful
    public PaymentResponse markPaymentSuccessful(
            Long id,
            String email) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(id));

        Booking booking = payment.getBooking();

        verifyBookingOwnership(booking, email);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {

            throw new PaymentStateTransitionException(
                    id,
                    payment.getPaymentStatus().name(),
                    PaymentStatus.SUCCESS.name()
            );
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        booking.setBookingStatus(BookingStatus.CONFIRMED);

        bookingRepository.save(booking);

        Payment savedPayment = paymentRepository.save(payment);

        notificationService.createNotification(
                booking.getUser().getId(),
                "Payment Successful",
                "Your payment for booking "
                        + booking.getBookingReference()
                        + " was successful."
        );

        return toResponse(savedPayment);
    }

    // Mark Payment as Failed
    public PaymentResponse markPaymentFailed(
            Long id,
            String email) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(id));

        Booking booking = payment.getBooking();

        verifyBookingOwnership(booking, email);

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {

            throw new PaymentStateTransitionException(
                    id,
                    payment.getPaymentStatus().name(),
                    PaymentStatus.FAILED.name()
            );
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);

        booking.setBookingStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);

        Payment savedPayment = paymentRepository.save(payment);

        notificationService.createNotification(
                booking.getUser().getId(),
                "Payment Failed",
                "Your payment for booking "
                        + booking.getBookingReference()
                        + " failed. Your booking has been cancelled."
        );

        return toResponse(savedPayment);
    }

    // Verify Booking Ownership
    private void verifyBookingOwnership(
            Booking booking,
            String email) {

        if (!booking.getUser().getEmail().equals(email)) {

            throw new BookingNotFoundException(
                    booking.getId()
            );
        }
    }

    // Convert Entity to Response
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

    // Generate Payment Reference
    private String generatePaymentReference() {

        return "PAY-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();
    }
}