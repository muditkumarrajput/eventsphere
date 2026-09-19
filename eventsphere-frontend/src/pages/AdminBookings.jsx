import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function AdminBookings() {
    const navigate = useNavigate();

    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchBookings = async () => {
            try {
                const response = await api.get(
                    "/bookings"
                );

                setBookings(response.data);
            } catch (error) {
                console.error(
                    "Failed to load all bookings:",
                    error
                );

                setError(
                    error.response?.data?.message ||
                    "Failed to load bookings."
                );
            } finally {
                setLoading(false);
            }
        };

        fetchBookings();
    }, []);

    if (loading) {
        return (
            <div className="bookings-page">

                <div className="bookings-header">
                    <h1>All Bookings</h1>

                    <p>
                        View and manage all EventSphere bookings.
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
                    All Bookings
                </h1>

                <p>
                    View all bookings made by EventSphere users.
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
                        No bookings found
                    </h2>

                    <p>
                        There are currently no bookings.
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

                    {bookings.map((booking) => (

                        <div
                            className="booking-summary-card"
                            key={booking.id}
                        >

                            <div className="booking-summary-header">

                                <h2>
                                    Booking #
                                    {booking.bookingReference}
                                </h2>

                                <span
                                    className={`booking-status ${booking.bookingStatus.toLowerCase()}`}
                                >
                                    {booking.bookingStatus}
                                </span>

                            </div>

                            <div className="booking-summary-info">

                                <div className="admin-booking-user">

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

                                </div>

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

                            <button
                                className="view-event-button"
                                onClick={() =>
                                    navigate(
                                        `/admin/bookings/${booking.id}`
                                    )
                                }
                            >
                                View Booking Details
                            </button>

                        </div>

                    ))}

                </div>
            )}

        </div>
    );
}

export default AdminBookings;
