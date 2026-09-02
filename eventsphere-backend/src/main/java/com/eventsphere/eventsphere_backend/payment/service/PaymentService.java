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

    @Transactional
    public PaymentResponse createPayment(
            CreatePaymentRequest request,
            String email) {

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() ->
                        new BookingNotFoundException(request.bookingId()));

        verifyBookingOwnership(booking, email);

        /*
         * A payment can only be created while the booking
         * is still pending.
         *
         * CANCELLED and CONFIRMED bookings must not create
         * another payment.
         */
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new PaymentStateTransitionException(
                    booking.getId(),
                    booking.getBookingStatus().name(),
                    BookingStatus.PENDING.name()
            );
        }

        if (paymentRepository.existsByBooking(booking)) {
            throw new PaymentAlreadyExistsException(
                    request.bookingId());
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
    // GET PAYMENT
    // =========================================================

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

    // =========================================================
    // MARK PAYMENT SUCCESSFUL
    // =========================================================

    @Transactional
    public PaymentResponse markPaymentSuccessful(
            Long id,
            String email) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(id));

        Booking booking = payment.getBooking();

        verifyBookingOwnership(
                booking,
                email
        );

        /*
         * Both payment and booking must be PENDING.
         *
         * This prevents a cancelled booking from becoming
         * confirmed through a late payment-success request.
         */
        if (payment.getPaymentStatus() != PaymentStatus.PENDING
                || booking.getBookingStatus() != BookingStatus.PENDING) {

            throw new PaymentStateTransitionException(
                    id,
                    payment.getPaymentStatus().name(),
                    PaymentStatus.SUCCESS.name()
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        booking.setBookingStatus(
                BookingStatus.CONFIRMED
        );

        bookingRepository.save(booking);

        Payment savedPayment =
                paymentRepository.save(payment);

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
    // MARK PAYMENT FAILED
    // =========================================================

    @Transactional
    public PaymentResponse markPaymentFailed(
            Long id,
            String email) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(id));

        Booking booking = payment.getBooking();

        verifyBookingOwnership(
                booking,
                email
        );

        /*
         * Both payment and booking must be PENDING.
         *
         * This prevents a payment from being marked FAILED
         * after the booking has already been cancelled.
         */
        if (payment.getPaymentStatus() != PaymentStatus.PENDING
                || booking.getBookingStatus() != BookingStatus.PENDING) {

            throw new PaymentStateTransitionException(
                    id,
                    payment.getPaymentStatus().name(),
                    PaymentStatus.FAILED.name()
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.FAILED
        );

        booking.setBookingStatus(
                BookingStatus.CANCELLED
        );

        bookingRepository.save(booking);

        Payment savedPayment =
                paymentRepository.save(payment);

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
    // REFUND PAYMENT
    // =========================================================

    @Transactional
    public PaymentResponse refundPayment(
            Long bookingId,
            String email) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(bookingId));

        verifyBookingOwnership(
                booking,
                email
        );

        Payment payment = paymentRepository
                .findByBooking(booking)
                .orElseThrow(() ->
                        new PaymentNotFoundException(bookingId));

        if (payment.getPaymentStatus()
                != PaymentStatus.SUCCESS) {

            throw new PaymentStateTransitionException(
                    payment.getId(),
                    payment.getPaymentStatus().name(),
                    PaymentStatus.REFUNDED.name()
            );
        }

        payment.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        payment.setPaymentDate(
                LocalDateTime.now()
        );

        Payment savedPayment =
                paymentRepository.save(payment);

        notificationService.createNotification(
                booking.getUser().getId(),
                "Payment Refunded",
                "Your payment for booking "
                        + booking.getBookingReference()
                        + " has been refunded."
        );

        return toResponse(savedPayment);
    }

    // =========================================================
    // OWNERSHIP VALIDATION
    // =========================================================

    private void verifyBookingOwnership(
            Booking booking,
            String email) {

        if (!booking.getUser()
                .getEmail()
                .equals(email)) {

            /*
             * Do not reveal that another user's booking/payment
             * exists.
             */
            throw new BookingNotFoundException(
                    booking.getId()
            );
        }
    }

    // =========================================================
    // PAYMENT RESPONSE MAPPER
    // =========================================================

    private PaymentResponse toResponse(
            Payment payment) {

        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(
                        payment.getPaymentReference()
                )
                .bookingId(
                        payment.getBooking().getId()
                )
                .amount(
                        payment.getAmount()
                )
                .paymentStatus(
                        payment.getPaymentStatus()
                )
                .paymentDate(
                        payment.getPaymentDate()
                )
                .createdAt(
                        payment.getCreatedAt()
                )
                .updatedAt(
                        payment.getUpdatedAt()
                )
                .build();
    }

    // =========================================================
    // PAYMENT REFERENCE GENERATOR
    // =========================================================

    private String generatePaymentReference() {

        return "PAY-"
                + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}