import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../services/api";

function Booking() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [event, setEvent] = useState(null);
    const [numberOfTickets, setNumberOfTickets] = useState(1);
    const [loading, setLoading] = useState(true);
    const [bookingLoading, setBookingLoading] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchEvent = async () => {
            try {
                const response = await api.get(
                    `/events/${id}`
                );

                setEvent(response.data);
            } catch (error) {
                console.error(
                    "Failed to load event:",
                    error
                );

                if (error.response?.data?.message) {
                    setError(
                        error.response.data.message
                    );
                } else {
                    setError(
                        "Failed to load event."
                    );
                }
            } finally {
                setLoading(false);
            }
        };

        fetchEvent();
    }, [id]);

    const handleBooking = async (event) => {
        event.preventDefault();

        setError("");

        if (
            !Number.isInteger(numberOfTickets) ||
            numberOfTickets < 1
        ) {
            setError(
                "Please enter at least 1 ticket."
            );
            return;
        }

        setBookingLoading(true);

        try {
            // =====================================================
            // CREATE BOOKING
            // =====================================================

            const bookingResponse = await api.post(
                "/bookings",
                {
                    eventId: Number(id),
                    numberOfTickets:
                        numberOfTickets,
                }
            );

            const booking =
                bookingResponse.data;

            // =====================================================
            // CREATE PAYMENT
            // =====================================================

            const paymentResponse = await api.post(
                "/payments",
                {
                    bookingId: booking.id,
                }
            );

            const payment =
                paymentResponse.data;

            // =====================================================
            // GO TO PAYMENT PAGE
            // =====================================================

            navigate(
                `/payments/${payment.id}`
            );

        } catch (error) {
            console.error(
                "Booking or payment creation failed:",
                error
            );

            if (error.response?.data?.message) {
                setError(
                    error.response.data.message
                );
            } else {
                setError(
                    "Failed to create booking. Please try again."
                );
            }
        } finally {
            setBookingLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="booking-page">

                <p>
                    Loading booking details...
                </p>

            </div>
        );
    }

    if (error && !event) {
        return (
            <div className="booking-page">

                <p className="favorites-error">
                    {error}
                </p>

                <button
                    className="back-button"
                    onClick={() =>
                        navigate("/events")
                    }
                >
                    ← Back to Events
                </button>

            </div>
        );
    }

    if (!event) {
        return (
            <div className="booking-page">

                <p className="favorites-error">
                    Event not found.
                </p>

                <button
                    className="back-button"
                    onClick={() =>
                        navigate("/events")
                    }
                >
                    ← Back to Events
                </button>

            </div>
        );
    }

    return (
        <div className="booking-page">

            <button
                className="back-button"
                onClick={() =>
                    navigate(`/events/${id}`)
                }
                disabled={bookingLoading}
            >
                ← Back to Event
            </button>

            <div className="booking-card">

                <h1>
                    Book Tickets
                </h1>

                {/* Event Information */}

                <div className="booking-event-info">

                    <h2>
                        {event.title}
                    </h2>

                    <p>
                        {event.description}
                    </p>

                    <p>
                        📍 {event.location}
                    </p>

                    <p>
                        📅{" "}
                        {new Date(
                            event.eventDate
                        ).toLocaleString()}
                    </p>

                    <p>
                        💰 ₹{event.ticketPrice} per ticket
                    </p>

                </div>

                {/* Booking Form */}

                <form onSubmit={handleBooking}>

                    <div className="ticket-input">

                        <label htmlFor="numberOfTickets">
                            Number of Tickets
                        </label>

                        <input
                            id="numberOfTickets"
                            type="number"
                            min="1"
                            step="1"
                            value={numberOfTickets}
                            onChange={(event) => {
                                const value =
                                    Number(
                                        event.target.value
                                    );

                                setNumberOfTickets(
                                    value
                                );
                            }}
                            disabled={bookingLoading}
                            required
                        />

                    </div>

                    {error && (
                        <p className="favorites-error">
                            {error}
                        </p>
                    )}

                    <button
                        type="submit"
                        className="book-button"
                        disabled={bookingLoading}
                    >
                        {bookingLoading
                            ? "Creating Booking..."
                            : "Continue to Payment"}
                    </button>

                </form>

            </div>

        </div>
    );
}

export default Booking;
