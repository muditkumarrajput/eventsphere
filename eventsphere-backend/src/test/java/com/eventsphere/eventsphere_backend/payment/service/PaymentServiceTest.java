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
import com.eventsphere.eventsphere_backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;


    // =========================================================
    // CREATE PAYMENT
    // =========================================================

    @Test
    void shouldCreatePaymentSuccessfully() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Booking booking = Booking.builder()
                .id(10L)
                .bookingReference("EVT-ABC12345")
                .totalAmount(new BigDecimal("2000.00"))
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .build();

        CreatePaymentRequest request =
                new CreatePaymentRequest(10L);

        Payment savedPayment = Payment.builder()
                .id(1L)
                .paymentReference("PAY-ABC12345")
                .amount(new BigDecimal("2000.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .booking(booking)
                .build();

        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.existsByBooking(booking))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        // Act
        PaymentResponse result =
                paymentService.createPayment(request, email);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals(
                "PAY-ABC12345",
                result.getPaymentReference()
        );
        assertEquals(10L, result.getBookingId());
        assertEquals(
                new BigDecimal("2000.00"),
                result.getAmount()
        );
        assertEquals(
                PaymentStatus.PENDING,
                result.getPaymentStatus()
        );

        verify(bookingRepository).findById(10L);
        verify(paymentRepository).existsByBooking(booking);
        verify(paymentRepository).save(any(Payment.class));
    }


    @Test
    void shouldThrowExceptionWhenBookingDoesNotExist() {

        // Arrange
        String email = "user@test.com";

        CreatePaymentRequest request =
                new CreatePaymentRequest(999L);

        when(bookingRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                BookingNotFoundException.class,
                () -> paymentService.createPayment(request, email)
        );

        verify(bookingRepository).findById(999L);
        verifyNoInteractions(paymentRepository);
    }


    @Test
    void shouldThrowExceptionWhenPaymentAlreadyExists() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Booking booking = Booking.builder()
                .id(10L)
                .totalAmount(new BigDecimal("2000.00"))
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .build();

        CreatePaymentRequest request =
                new CreatePaymentRequest(10L);

        when(bookingRepository.findById(10L))
                .thenReturn(Optional.of(booking));

        when(paymentRepository.existsByBooking(booking))
                .thenReturn(true);

        // Act + Assert
        assertThrows(
                PaymentAlreadyExistsException.class,
                () -> paymentService.createPayment(request, email)
        );

        verify(paymentRepository).existsByBooking(booking);
        verify(paymentRepository, never())
                .save(any(Payment.class));
    }


    // =========================================================
    // GET PAYMENT
    // =========================================================

    @Test
    void shouldGetPaymentByIdSuccessfully() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Booking booking = Booking.builder()
                .id(10L)
                .user(user)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .paymentReference("PAY-ABC12345")
                .amount(new BigDecimal("2000.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .booking(booking)
                .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        // Act
        PaymentResponse result =
                paymentService.getPaymentById(1L, email);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals(
                "PAY-ABC12345",
                result.getPaymentReference()
        );
        assertEquals(10L, result.getBookingId());
        assertEquals(
                PaymentStatus.PENDING,
                result.getPaymentStatus()
        );

        verify(paymentRepository).findById(1L);
    }


    @Test
    void shouldThrowExceptionWhenPaymentDoesNotExist() {

        // Arrange
        String email = "user@test.com";

        when(paymentRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.getPaymentById(999L, email)
        );

        verify(paymentRepository).findById(999L);
    }


    @Test
    void shouldNotAllowUserToAccessAnotherUsersPayment() {

        // Arrange
        String ownerEmail = "owner@test.com";
        String otherEmail = "other@test.com";

        User owner = new User();
        owner.setId(5L);
        owner.setEmail(ownerEmail);

        Booking booking = Booking.builder()
                .id(10L)
                .user(owner)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .paymentReference("PAY-ABC12345")
                .amount(new BigDecimal("2000.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .booking(booking)
                .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        // Act + Assert
        assertThrows(
                BookingNotFoundException.class,
                () -> paymentService.getPaymentById(1L, otherEmail)
        );

        verify(paymentRepository).findById(1L);
    }


    // =========================================================
    // PAYMENT SUCCESS
    // =========================================================

    @Test
    void shouldMarkPaymentSuccessful() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Booking booking = Booking.builder()
                .id(10L)
                .bookingReference("EVT-ABC12345")
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .paymentReference("PAY-ABC12345")
                .amount(new BigDecimal("2000.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .booking(booking)
                .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        // Act
        PaymentResponse result =
                paymentService.markPaymentSuccessful(1L, email);

        // Assert
        assertEquals(
                PaymentStatus.SUCCESS,
                payment.getPaymentStatus()
        );

        assertEquals(
                BookingStatus.CONFIRMED,
                booking.getBookingStatus()
        );

        assertEquals(
                PaymentStatus.SUCCESS,
                result.getPaymentStatus()
        );

        verify(bookingRepository).save(booking);
        verify(paymentRepository).save(payment);

        verify(notificationService).createNotification(
                5L,
                "Payment Successful",
                "Your payment for booking EVT-ABC12345 was successful."
        );
    }


    @Test
    void shouldNotMarkAlreadySuccessfulPaymentAsSuccessfulAgain() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Booking booking = Booking.builder()
                .id(10L)
                .user(user)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .paymentStatus(PaymentStatus.SUCCESS)
                .booking(booking)
                .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        // Act + Assert
        assertThrows(
                PaymentStateTransitionException.class,
                () -> paymentService.markPaymentSuccessful(1L, email)
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verifyNoInteractions(notificationService);
    }


    // =========================================================
    // PAYMENT FAILED
    // =========================================================

    @Test
    void shouldMarkPaymentFailed() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Booking booking = Booking.builder()
                .id(10L)
                .bookingReference("EVT-ABC12345")
                .bookingStatus(BookingStatus.PENDING)
                .user(user)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .paymentReference("PAY-ABC12345")
                .amount(new BigDecimal("2000.00"))
                .paymentStatus(PaymentStatus.PENDING)
                .booking(booking)
                .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        // Act
        PaymentResponse result =
                paymentService.markPaymentFailed(1L, email);

        // Assert
        assertEquals(
                PaymentStatus.FAILED,
                payment.getPaymentStatus()
        );

        assertEquals(
                BookingStatus.CANCELLED,
                booking.getBookingStatus()
        );

        assertEquals(
                PaymentStatus.FAILED,
                result.getPaymentStatus()
        );

        verify(bookingRepository).save(booking);
        verify(paymentRepository).save(payment);

        verify(notificationService).createNotification(
                5L,
                "Payment Failed",
                "Your payment for booking EVT-ABC12345 failed. Your booking has been cancelled."
        );
    }


    @Test
    void shouldNotMarkAlreadyFailedPaymentAsFailedAgain() {

        // Arrange
        String email = "user@test.com";

        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        Booking booking = Booking.builder()
                .id(10L)
                .user(user)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .paymentStatus(PaymentStatus.FAILED)
                .booking(booking)
                .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        // Act + Assert
        assertThrows(
                PaymentStateTransitionException.class,
                () -> paymentService.markPaymentFailed(1L, email)
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verifyNoInteractions(notificationService);
    }


    // =========================================================
    // PAYMENT OWNERSHIP
    // =========================================================

    @Test
    void shouldNotAllowAnotherUserToMarkPaymentSuccessful() {

        // Arrange
        String ownerEmail = "owner@test.com";
        String otherEmail = "other@test.com";

        User owner = new User();
        owner.setId(5L);
        owner.setEmail(ownerEmail);

        Booking booking = Booking.builder()
                .id(10L)
                .user(owner)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .paymentStatus(PaymentStatus.PENDING)
                .booking(booking)
                .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        // Act + Assert
        assertThrows(
                BookingNotFoundException.class,
                () -> paymentService.markPaymentSuccessful(1L, otherEmail)
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verifyNoInteractions(notificationService);
    }


    @Test
    void shouldNotAllowAnotherUserToMarkPaymentFailed() {

        // Arrange
        String ownerEmail = "owner@test.com";
        String otherEmail = "other@test.com";

        User owner = new User();
        owner.setId(5L);
        owner.setEmail(ownerEmail);

        Booking booking = Booking.builder()
                .id(10L)
                .user(owner)
                .build();

        Payment payment = Payment.builder()
                .id(1L)
                .paymentStatus(PaymentStatus.PENDING)
                .booking(booking)
                .build();

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        // Act + Assert
        assertThrows(
                BookingNotFoundException.class,
                () -> paymentService.markPaymentFailed(1L, otherEmail)
        );

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verifyNoInteractions(notificationService);
    }
}
