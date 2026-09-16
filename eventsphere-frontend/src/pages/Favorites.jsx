import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";

function Favorites() {
    const [favorites, setFavorites] = useState([]);
    const [loading, setLoading] = useState(true);
    const [removingEventId, setRemovingEventId] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchFavorites = async () => {
            try {
                const response = await api.get("/favorites");

                setFavorites(response.data);
            } catch (error) {
                console.error(
                    "Failed to load favorites:",
                    error
                );

                if (error.response?.data?.message) {
                    setError(
                        error.response.data.message
                    );
                } else {
                    setError(
                        "Failed to load favorites."
                    );
                }
            } finally {
                setLoading(false);
            }
        };

        fetchFavorites();
    }, []);

    const removeFavorite = async (eventId) => {
        setError("");
        setRemovingEventId(eventId);

        try {
            await api.delete(
                `/favorites/${eventId}`
            );

            setFavorites((currentFavorites) =>
                currentFavorites.filter(
                    (favorite) =>
                        favorite.eventId !== eventId
                )
            );
        } catch (error) {
            console.error(
                "Failed to remove favorite:",
                error
            );

            if (error.response?.data?.message) {
                setError(
                    error.response.data.message
                );
            } else {
                setError(
                    "Failed to remove favorite."
                );
            }
        } finally {
            setRemovingEventId(null);
        }
    };

    if (loading) {
        return (
            <div className="favorites-page">

                <div className="favorites-header">
                    <h1>My Favorites</h1>

                    <p>
                        Events you have saved for later.
                    </p>
                </div>

                <p className="favorites-message">
                    Loading favorites...
                </p>

            </div>
        );
    }

    return (
        <div className="favorites-page">

            <div className="favorites-header">

                <h1>
                    My Favorites
                </h1>

                <p>
                    Events you have saved for later.
                </p>

            </div>

            {error && (
                <p className="favorites-error">
                    {error}
                </p>
            )}

            {favorites.length === 0 ? (
                <div className="empty-favorites">

                    <h2>
                        No favorites yet
                    </h2>

                    <p>
                        You haven't added any events
                        to your favorites.
                    </p>

                    <Link
                        to="/events"
                        className="view-event-button"
                    >
                        Explore Events
                    </Link>

                </div>
            ) : (
                <div className="favorites-grid">

                    {favorites.map((favorite) => (

                        <div
                            className="favorite-card"
                            key={favorite.id}
                        >

                            <div className="favorite-card-content">

                                <h2>
                                    {favorite.eventTitle}
                                </h2>

                                <p className="favorite-location">
                                    📍{" "}
                                    {favorite.eventLocation}
                                </p>

                                <p className="favorite-date">
                                    📅{" "}
                                    {new Date(
                                        favorite.eventDate
                                    ).toLocaleString()}
                                </p>

                                <p className="favorite-added">
                                    Added to favorites:{" "}
                                    {new Date(
                                        favorite.createdAt
                                    ).toLocaleDateString()}
                                </p>

                                <Link
                                    to={`/events/${favorite.eventId}`}
                                    className="view-event-button"
                                >
                                    View Event
                                </Link>

                            </div>

                            <button
                                className="remove-favorite-button"
                                onClick={() =>
                                    removeFavorite(
                                        favorite.eventId
                                    )
                                }
                                disabled={
                                    removingEventId ===
                                    favorite.eventId
                                }
                            >
                                {removingEventId ===
                                favorite.eventId
                                    ? "Removing..."
                                    : "Remove Favorite"}
                            </button>

                        </div>

                    ))}

                </div>
            )}

        </div>
    );
}

export default Favorites;
