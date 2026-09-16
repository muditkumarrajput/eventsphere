import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../services/api";

function BookingDetails() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [booking, setBooking] = useState(null);
    const [loading, setLoading] = useState(true);
    const [cancelling, setCancelling] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchBooking = async () => {
            try {
                const response = await api.get(
                    `/bookings/${id}`
                );

                setBooking(response.data);
            } catch (error) {
                console.error(
                    "Failed to load booking:",
                    error
                );

                if (error.response?.data?.message) {
                    setError(
                        error.response.data.message
                    );
                } else {
                    setError(
                        "Failed to load booking."
                    );
                }
            } finally {
                setLoading(false);
            }
        };

        fetchBooking();
    }, [id]);

    const cancelBooking = async () => {
        const confirmed = window.confirm(
            "Are you sure you want to cancel this booking?"
        );

        if (!confirmed) {
            return;
        }

        setError("");
        setCancelling(true);

        try {
            await api.delete(
                `/bookings/${id}`
            );

            setBooking((currentBooking) => ({
                ...currentBooking,
                bookingStatus:
                    "CANCELLED",
            }));
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
            setCancelling(false);
        }
    };

    if (loading) {
        return (
            <div className="booking-details-page">

                <p>
                    Loading booking...
                </p>

            </div>
        );
    }

    if (error || !booking) {
        return (
            <div className="booking-details-page">

                <p className="favorites-error">
                    {error ||
                        "Booking not found."}
                </p>

                <button
                    className="back-button"
                    onClick={() =>
                        navigate("/bookings")
                    }
                >
                    ← Back to My Bookings
                </button>

            </div>
        );
    }

    return (
        <div className="booking-details-page">

            <button
                className="back-button"
                onClick={() =>
                    navigate("/bookings")
                }
                disabled={cancelling}
            >
                ← Back to My Bookings
            </button>

            <div className="booking-details-card">

                {/* Booking Header */}

                <div className="booking-details-header">

                    <h1>
                        Booking Details
                    </h1>

                    <span
                        className={`booking-status ${booking.bookingStatus.toLowerCase()}`}
                    >
                        {booking.bookingStatus}
                    </span>

                </div>

                {error && (
                    <p className="favorites-error">
                        {error}
                    </p>
                )}

                {/* Event Information */}

                <div className="booking-event-details">

                    <h2>
                        {booking.eventTitle}
                    </h2>

                    <p>
                        📍{" "}
                        {booking.eventLocation}
                    </p>

                    <p>
                        📅{" "}
                        {new Date(
                            booking.eventDate
                        ).toLocaleString()}
                    </p>

                </div>

                {/* Booking Reference */}

                <div className="booking-reference">

                    <span>
                        Booking Reference
                    </span>

                    <strong>
                        {booking.bookingReference}
                    </strong>

                </div>

                {/* Booking Information */}

                <div className="booking-details-info">

                    <div>
                        <span>
                            Number of Tickets
                        </span>

                        <strong>
                            {booking.numberOfTickets}
                        </strong>
                    </div>

                    <div>
                        <span>
                            Total Amount
                        </span>

                        <strong>
                            ₹{booking.totalAmount}
                        </strong>
                    </div>

                    <div>
                        <span>
                            Booking Date
                        </span>

                        <strong>
                            {new Date(
                                booking.bookingDate
                            ).toLocaleString()}
                        </strong>
                    </div>

                    <div>
                        <span>
                            Created At
                        </span>

                        <strong>
                            {new Date(
                                booking.createdAt
                            ).toLocaleString()}
                        </strong>
                    </div>

                    <div>
                        <span>
                            Last Updated
                        </span>

                        <strong>
                            {new Date(
                                booking.updatedAt
                            ).toLocaleString()}
                        </strong>
                    </div>

                    <div>
                        <span>
                            Event ID
                        </span>

                        <strong>
                            {booking.eventId}
                        </strong>
                    </div>

                </div>

                {/* Cancel Booking */}

                {booking.bookingStatus !==
                    "CANCELLED" && (
                    <button
                        className="cancel-booking-button"
                        onClick={cancelBooking}
                        disabled={cancelling}
                    >
                        {cancelling
                            ? "Cancelling..."
                            : "Cancel Booking"}
                    </button>
                )}

            </div>

        </div>
    );
}

export default BookingDetails;
