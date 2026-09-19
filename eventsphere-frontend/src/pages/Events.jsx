import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";

function Events() {
    const [events, setEvents] = useState([]);
    const [favoriteEventIds, setFavoriteEventIds] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const loadEvents = async () => {
            try {
                const response = await api.get("/events");
                setEvents(response.data);
            } catch (error) {
                console.error(
                    "Failed to load events:",
                    error
                );

                setError("Failed to load events.");
            } finally {
                setLoading(false);
            }
        };

        const loadFavorites = async () => {
            try {
                const response = await api.get("/favorites");

                const favoriteIds = response.data.map(
                    (favorite) => favorite.eventId
                );

                setFavoriteEventIds(favoriteIds);
            } catch (error) {
                console.error(
                    "Failed to load favorites:",
                    error
                );
            }
        };

        loadEvents();
        loadFavorites();
    }, []);

    const toggleFavorite = async (eventId) => {
        const isFavorite =
            favoriteEventIds.includes(eventId);

        try {
            if (isFavorite) {
                await api.delete(
                    `/favorites/${eventId}`
                );

                setFavoriteEventIds((currentIds) =>
                    currentIds.filter(
                        (id) => id !== eventId
                    )
                );
            } else {
                await api.post(
                    `/favorites/${eventId}`
                );

                setFavoriteEventIds((currentIds) => [
                    ...currentIds,
                    eventId,
                ]);
            }
        } catch (error) {
            console.error(
                "Failed to update favorite:",
                error
            );

            setError(
                "Failed to update favorite."
            );
        }
    };

    if (loading) {
        return <p>Loading events...</p>;
    }

    if (error && events.length === 0) {
        return <p>{error}</p>;
    }

    return (
        <div className="events-page">
            <h1>Events</h1>

            {error && (
                <p className="favorites-error">
                    {error}
                </p>
            )}

            {events.length === 0 ? (
                <p>No events available.</p>
            ) : (
                <div className="events-grid">
                    {events.map((event) => {
                        const isFavorite =
                            favoriteEventIds.includes(
                                event.id
                            );

                        return (
                            <div
                                className="event-card"
                                key={event.id}
                            >
                                <div>
                                    <h2>{event.title}</h2>

                                    <p>
                                        {event.description}
                                    </p>

                                    <p>
                                        📍 {event.location}
                                    </p>

                                    <p>
                                        💰 ₹
                                        {event.ticketPrice}
                                    </p>

                                    <Link
                                        className="view-event-button"
                                        to={`/events/${event.id}`}
                                    >
                                        View Details
                                    </Link>
                                </div>

                                <button
                                    className={
                                        isFavorite
                                            ? "favorite-button active"
                                            : "favorite-button"
                                    }
                                    onClick={() =>
                                        toggleFavorite(
                                            event.id
                                        )
                                    }
                                >
                                    {isFavorite
                                        ? "♥ Favorited"
                                        : "♡ Add to Favorites"}
                                </button>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}

export default Events;