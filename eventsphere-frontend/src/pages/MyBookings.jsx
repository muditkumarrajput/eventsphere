import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function MyBookings() {
    const navigate = useNavigate();

    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [cancellingBookingId, setCancellingBookingId] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchBookings = async () => {
            try {
                const response = await api.get(
                    "/bookings/my"
                );

                setBookings(response.data);
            } catch (error) {
                console.error(
                    "Failed to load bookings:",
                    error
                );

                if (error.response?.data?.message) {
                    setError(
                        error.response.data.message
                    );
                } else {
                    setError(
                        "Failed to load your bookings."
                    );
                }
            } finally {
                setLoading(false);
            }
        };

        fetchBookings();
    }, []);

    const cancelBooking = async (bookingId) => {
        const confirmed = window.confirm(
            "Are you sure you want to cancel this booking?"
        );

        if (!confirmed) {
            return;
        }

        setError("");
        setCancellingBookingId(bookingId);

        try {
            await api.delete(
                `/bookings/${bookingId}`
            );

            setBookings((currentBookings) =>
                currentBookings.map((booking) =>
                    booking.id === bookingId
                        ? {
                              ...booking,
                              bookingStatus:
                                  "CANCELLED",
                          }
                        : booking
                )
            );
        } catch (error) {
            console.error(
                "Failed to cancel booking:",
                error
            );

            if (error.response?.data?.message) {
                setError(
                    error.response.data.message
                );
            } else {
                setError(
                    "Failed to cancel booking."
                );
            }
        } finally {
            setCancellingBookingId(null);
        }
    };

    if (loading) {
        return (
            <div className="bookings-page">

                <div className="bookings-header">
                    <h1>
                        My Bookings
                    </h1>

                    <p>
                        View and manage your event
                        bookings.
                    </p>
                </div>

                <p className="bookings-message">
                    Loading bookings...
                </p>

            </div>
        );
    }

    return (
        <div className="bookings-page">

            <div className="bookings-header">

                <h1>
                    My Bookings
                </h1>

                <p>
                    View and manage your event
                    bookings.
                </p>

            </div>

            {error && (
                <p className="favorites-error">
                    {error}
                </p>
            )}

            {bookings.length === 0 ? (
                <div className="empty-bookings">

                    <h2>
                        No bookings yet
                    </h2>

                    <p>
                        You haven't booked any events
                        yet.
                    </p>

                    <button
                        className="book-button"
                        onClick={() =>
                            navigate("/events")
                        }
                    >
                        Browse Events
                    </button>

                </div>
            ) : (
                <div className="bookings-grid">

                    {bookings.map((booking) => {

                        const isCancelling =
                            cancellingBookingId ===
                            booking.id;

                        return (
                            <div
                                className="booking-summary-card"
                                key={booking.id}
                            >

                                <div className="booking-summary-header">

                                    <h2>
                                        <button
                                            className="booking-reference-button"
                                            onClick={() =>
                                                navigate(
                                                    `/bookings/${booking.id}`
                                                )
                                            }
                                        >
                                            Booking #
                                            {booking.bookingReference}
                                        </button>
                                    </h2>

                                    <span
                                        className={`booking-status ${booking.bookingStatus.toLowerCase()}`}
                                    >
                                        {booking.bookingStatus}
                                    </span>

                                </div>

                                <div className="booking-summary-info">

                                    {/* Event Information */}

                                    <p>
                                        <strong>
                                            Event:
                                        </strong>{" "}
                                        {booking.eventTitle}
                                    </p>

                                    <p>
                                        <strong>
                                            Location:
                                        </strong>{" "}
                                        {booking.eventLocation}
                                    </p>

                                    <p>
                                        <strong>
                                            Event Date:
                                        </strong>{" "}
                                        {new Date(
                                            booking.eventDate
                                        ).toLocaleString()}
                                    </p>

                                    {/* Booking Information */}

                                    <p>
                                        <strong>
                                            Tickets:
                                        </strong>{" "}
                                        {booking.numberOfTickets}
                                    </p>

                                    <p>
                                        <strong>
                                            Total Amount:
                                        </strong>{" "}
                                        ₹{booking.totalAmount}
                                    </p>

                                    <p>
                                        <strong>
                                            Booking Date:
                                        </strong>{" "}
                                        {new Date(
                                            booking.bookingDate
                                        ).toLocaleString()}
                                    </p>

                                    <p>
                                        <strong>
                                            Created:
                                        </strong>{" "}
                                        {new Date(
                                            booking.createdAt
                                        ).toLocaleString()}
                                    </p>

                                </div>

                                {booking.bookingStatus !==
                                    "CANCELLED" && (
                                    <button
                                        className="cancel-booking-button"
                                        onClick={() =>
                                            cancelBooking(
                                                booking.id
                                            )
                                        }
                                        disabled={
                                            isCancelling
                                        }
                                    >
                                        {isCancelling
                                            ? "Cancelling..."
                                            : "Cancel Booking"}
                                    </button>
                                )}

                            </div>
                        );
                    })}

                </div>
            )}

        </div>
    );
}

export default MyBookings;
