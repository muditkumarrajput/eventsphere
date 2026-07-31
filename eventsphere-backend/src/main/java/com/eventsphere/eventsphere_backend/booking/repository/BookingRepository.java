package com.eventsphere.eventsphere_backend.booking.repository;

import com.eventsphere.eventsphere_backend.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    @Query("""
            SELECT COALESCE(SUM(b.numberOfTickets), 0)
            FROM Booking b
            WHERE b.event.id = :eventId
            AND b.bookingStatus = 'CONFIRMED'
            """)
    Integer getBookedTickets(Long eventId);

    List<Booking> findAllByOrderByCreatedAtDesc();

}