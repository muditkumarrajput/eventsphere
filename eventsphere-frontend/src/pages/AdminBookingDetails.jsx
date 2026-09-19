import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../services/api";

function AdminBookingDetails() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [booking, setBooking] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchBooking = async () => {
            try {
                const response = await api.get(
                    `/bookings/admin/${id}`
                );

                setBooking(response.data);
            } catch (error) {
                console.error(
                    "Failed to load booking details:",
                    error
                );

                setError(
                    error.response?.data?.message ||
                    "Failed to load booking details."
                );
            } finally {
                setLoading(false);
            }
        };

        fetchBooking();
    }, [id]);

    if (loading) {
        return (
            <div className="booking-page">
                <p>
                    Loading booking details...
                </p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="booking-page">

                <p className="favorites-error">
                    {error}
                </p>

                <button
                    className="back-button"
                    onClick={() =>
                        navigate("/admin/bookings")
                    }
                >
                    ← Back to All Bookings
                </button>

            </div>
        );
    }

    if (!booking) {
        return (
            <div className="booking-page">

                <p className="favorites-error">
                    Booking not found.
                </p>

                <button
                    className="back-button"
                    onClick={() =>
                        navigate("/admin/bookings")
                    }
                >
                    ← Back to All Bookings
                </button>

            </div>
        );
    }

    return (
        <div className="booking-page">

            <button
                className="back-button"
                onClick={() =>
                    navigate("/admin/bookings")
                }
            >
                ← Back to All Bookings
            </button>

            <div className="booking-card">

                <h1>
                    Booking Details
                </h1>

                <div className="booking-event-info">

                    <h2>
                        #{booking.bookingReference}
                    </h2>

                    <p>
                        <strong>
                            Status:
                        </strong>{" "}
                        {booking.bookingStatus}
                    </p>

                </div>

                <div className="booking-details-info">

                    <h3>
                        👤 Customer
                    </h3>

                    <p>
                        <strong>
                            Name:
                        </strong>{" "}
                        {booking.userName}
                    </p>

                    <p>
                        <strong>
                            Email:
                        </strong>{" "}
                        {booking.userEmail}
                    </p>

                    <h3>
                        🎫 Event
                    </h3>

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

                    <h3>
                        💳 Booking
                    </h3>

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

            </div>

        </div>
    );
}

export default AdminBookingDetails;
