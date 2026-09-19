import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../services/api";
import { useAuth } from "../context/useAuth.jsx";

function EventDetails() {
    const { id } = useParams();
    const navigate = useNavigate();

    const { role } = useAuth();

    const [event, setEvent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const normalizedRoles =
        Array.isArray(role)
            ? role
            : [role];

    const isUser =
        normalizedRoles.some((userRole) => {
            const normalizedRole =
                String(userRole)
                    .replace("ROLE_", "")
                    .toUpperCase();

            return normalizedRole === "USER";
        });

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

                setError(
                    "Failed to load event."
                );
            } finally {
                setLoading(false);
            }
        };

        fetchEvent();
    }, [id]);

    if (loading) {
        return (
            <div className="event-details-page">
                <p>Loading event...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="event-details-page">

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
            <div className="event-details-page">
                <p>Event not found.</p>
            </div>
        );
    }

    return (
        <div className="event-details-page">

            <button
                className="back-button"
                onClick={() =>
                    navigate("/events")
                }
            >
                ← Back to Events
            </button>

            <div className="event-details-card">

                <h1>
                    {event.title}
                </h1>

                <p className="event-description">
                    {event.description}
                </p>

                <div className="event-details-info">

                    <p>
                        <strong>
                            📍 Location:
                        </strong>{" "}
                        {event.location}
                    </p>

                    <p>
                        <strong>
                            📅 Date:
                        </strong>{" "}
                        {new Date(
                            event.eventDate
                        ).toLocaleString()}
                    </p>

                    <p>
                        <strong>
                            💰 Ticket Price:
                        </strong>{" "}
                        ₹{event.ticketPrice}
                    </p>

                </div>

                {isUser && (
                    <button
                        className="book-button"
                        onClick={() =>
                            navigate(
                                `/events/${event.id}/book`
                            )
                        }
                    >
                        Book Tickets
                    </button>
                )}

            </div>

        </div>
    );
}

export default EventDetails;